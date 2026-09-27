package it.cityvoice.api.features.districts.repositories;

import it.cityvoice.api.features.districts.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepo extends JpaRepository<District, Long> {

}