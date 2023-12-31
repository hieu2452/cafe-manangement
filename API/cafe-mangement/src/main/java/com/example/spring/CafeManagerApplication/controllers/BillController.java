package com.example.spring.CafeManagerApplication.controllers;

import com.example.spring.CafeManagerApplication.dto.BillDetailDto;
import com.example.spring.CafeManagerApplication.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bill")
public class BillController {
    @Autowired
    private BillService billService;

    @PostMapping("add")
    public ResponseEntity<?> addNewBill(@RequestBody BillDetailDto billDetailDto){
        return billService.addNewBill(billDetailDto);
    }

    @GetMapping("get")
    public ResponseEntity<?> getBill(){
        return billService.getAllBill();
    }

    @GetMapping("get/{id}")
    public ResponseEntity<?> getBillById(@PathVariable Integer id) {
        return billService.getBillById(id);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable Integer id) {
        return billService.deleteBill(id);
    }

}
