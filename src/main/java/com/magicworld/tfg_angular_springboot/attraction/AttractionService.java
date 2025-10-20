package com.magicworld.tfg_angular_springboot.attraction;

import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttractionService {

    private final AttractionRepository attractionRepository;

    @Transactional
    public Attraction saveAttraction(Attraction attraction) {
        return attractionRepository.save(attraction);
    }

    @Transactional(readOnly = true)
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("error.attraction.notfound"));
    }

    @Transactional
    public void deleteAttraction(Long id) {
        Attraction attraction = getAttractionById(id);
        attractionRepository.delete(attraction);
    }

    @Transactional
    public Attraction updateAttraction(Long id, Attraction updatedAttraction) {
        Attraction existingAttraction = getAttractionById(id);

        existingAttraction.setName(updatedAttraction.getName());
        existingAttraction.setIntensity(updatedAttraction.getIntensity());
        existingAttraction.setMinimumHeight(updatedAttraction.getMinimumHeight());
        existingAttraction.setMinimumAge(updatedAttraction.getMinimumAge());
        existingAttraction.setMinimumWeight(updatedAttraction.getMinimumWeight());
        existingAttraction.setDescription(updatedAttraction.getDescription());
        existingAttraction.setPhotoUrl(updatedAttraction.getPhotoUrl());
        existingAttraction.setIsActive(updatedAttraction.getIsActive());
        return attractionRepository.save(existingAttraction);
    }
}
