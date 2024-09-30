package com.example.backend.controller;

import com.example.backend.entity.Cart;
import com.example.backend.entity.Item;
import com.example.backend.repositoty.CartRepository;
import com.example.backend.repositoty.ItemRepository;
import com.example.backend.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    JwtService jwtService;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ItemRepository itemRepository;

    // 장바구니의 모든 상품 조회
    @GetMapping("/api/cart/items")
    public ResponseEntity getCartItems(@CookieValue(value = "token", required = false) String token) {

        // JWT 토큰이 유효하지 않으면 401 UNAUTHORIZED 에러를 발생시킴
        if(!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // JWT 토큰에서 사용자 ID를 추출
        int memberId = jwtService.getId(token);

        // 해당 사용자의 장바구니 항목들을 조회
        List<Cart> carts = cartRepository.findByMemberId(memberId);

        // 장바구니에서 해당하는 상품 ID들만 추출
        List<Integer> itemIds = carts.stream().map(Cart::getItemId).toList();

        // 상품 ID들을 기준으로 상품 목록을 조회
        List<Item> items = itemRepository.findByIdIn(itemIds);

        return new ResponseEntity<>(items, HttpStatus.OK);
    }

    // 장바구니에 상품 추가
    @PostMapping("/api/cart/items/{itemId}")
    public ResponseEntity pushCartItem (
            @PathVariable("itemId") int itemId,
            @CookieValue(value = "token", required = false) String token
    ) {

        if(!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // JWt 토콘에서 사용자 ID를 추출
        int memberId = jwtService.getId(token);

        // 해당 사용자의 장바구니에서 해당 상품이 이미 존재하는지 확인
        Cart cart = cartRepository.findByMemberIdAndItemId(memberId, itemId);

        // 해당 상품이 장바구니에 없으면, 새로운 항목을 생성하여 저장
        if(cart == null) {
            Cart newCart = new Cart();
            newCart.setMemberId(memberId);
            newCart.setItemId(itemId);
            cartRepository.save(newCart); // 새로운 장바구니 항목을 데이터베이스에 저장
        }

        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 장바구니에서 상품 삭제
    @DeleteMapping("/api/cart/items/{itemId}")
    public ResponseEntity removeCartItem (
            @PathVariable("itemId") int itemId,
            @CookieValue(value = "token", required = false) String token
    ) {
        if(!jwtService.isValid(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        int memberId = jwtService.getId(token);
        Cart cart = cartRepository.findByMemberIdAndItemId(memberId, itemId);

        cartRepository.delete(cart);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}