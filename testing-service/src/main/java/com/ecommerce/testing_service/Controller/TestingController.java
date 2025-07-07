package com.ecommerce.testing_service.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.testing_service.DTO.TestingCreate;
import com.ecommerce.testing_service.DTO.TestingResponse;
import com.ecommerce.testing_service.Service.TestingService;

@RestController
@RequestMapping("/testing")
public class TestingController {
    @Autowired
    private TestingService testingService;

    // 🔒 USER: hanya dapat data milik sendiri
    @GetMapping("/my")
    public ResponseEntity<List<TestingResponse>> getMyData() {
        List<TestingResponse> list = testingService.getAllTestingByUser();
        return ResponseEntity.ok(list);
    }

    // CREATE
    @PostMapping
    public ResponseEntity<TestingResponse> create(@RequestBody TestingCreate request) {
        TestingResponse response = testingService.createTesting(request);
        return ResponseEntity.ok(response);
    }

    // 🔓 ADMIN & KASIR: melihat semua data, USER: hanya data sendiri
    @GetMapping
    public ResponseEntity<List<TestingResponse>> getAll() {
        List<TestingResponse> list = testingService.getAllTesting();
        return ResponseEntity.ok(list);
    }

    // 🔒 ADMIN, KASIR: bisa lihat semua, USER: hanya jika ID miliknya
    @GetMapping("/{id}")
    public ResponseEntity<TestingResponse> getById(@PathVariable int id) {
        TestingResponse response = testingService.getById(id);
        return ResponseEntity.ok(response);
    }

    // 🔒 USER: bisa update data sendiri, KASIR: update status saja
    @PutMapping("/{id}")
    public ResponseEntity<TestingResponse> update(@PathVariable int id, @RequestBody TestingCreate request) {
        TestingResponse response = testingService.updateTesting(id, request);
        return ResponseEntity.ok(response);
    }

    // 🔒 USER: hapus sendiri, ADMIN: bisa semua, KASIR: dilarang hapus
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        testingService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
