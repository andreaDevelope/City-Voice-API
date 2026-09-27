package it.cityvoice.api.features.districts.controllers;

import it.cityvoice.api.features.districts.dto.MunicipioGroupResponse;
import it.cityvoice.api.features.districts.services.DistrictServ;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cityvoice/districts")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class PublicDistrictController {

    private final DistrictServ districtServ;

    @GetMapping
    public ResponseEntity<List<MunicipioGroupResponse>> getDistricts() {
        return ResponseEntity.ok(districtServ.getGroupedByMunicipio());
    }
}