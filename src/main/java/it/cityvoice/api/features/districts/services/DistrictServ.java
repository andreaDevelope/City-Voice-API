package it.cityvoice.api.features.districts.services;

import it.cityvoice.api.features.districts.dto.DistrictResponse;
import it.cityvoice.api.features.districts.dto.MunicipioGroupResponse;
import it.cityvoice.api.features.districts.entity.District;
import it.cityvoice.api.features.districts.enums.Municipio;
import it.cityvoice.api.features.districts.repositories.DistrictRepo;
import it.cityvoice.api.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DistrictServ {

    private final DistrictRepo districtRepo;

    public District getById(Long districtId) {
        return districtRepo.findById(districtId)
                .orElseThrow(() -> new ResourceNotFoundException("Quartiere non trovato"));
    }

    // quartieri raggruppati per municipio, nell'ordine di dichiarazione dell'enum
    public List<MunicipioGroupResponse> getGroupedByMunicipio() {
        Map<Municipio, List<District>> byMunicipio = districtRepo.findAll().stream()
                .collect(Collectors.groupingBy(District::getMunicipio));

        return byMunicipio.keySet().stream()
                .sorted(Comparator.comparingInt(Municipio::ordinal))
                .map(municipio -> toGroup(municipio, byMunicipio.get(municipio)))
                .toList();
    }

    private MunicipioGroupResponse toGroup(Municipio municipio, List<District> districts) {
        List<DistrictResponse> sorted = districts.stream()
                .sorted(Comparator.comparing(District::getName))
                .map(DistrictResponse::from)
                .toList();

        return new MunicipioGroupResponse(municipio, municipio.getLabel(), sorted);
    }
}