package com.magicworld.tfg_angular_springboot.park_closure;

import com.magicworld.tfg_angular_springboot.exceptions.BadRequestException;
import com.magicworld.tfg_angular_springboot.exceptions.InvalidOperationException;
import com.magicworld.tfg_angular_springboot.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkClosureDayService {

    private final ParkClosureDayRepository repository;

    @Transactional(readOnly = true)
    public List<ParkClosureDay> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public ParkClosureDay findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.closure.notfound"));
    }

    @Transactional(readOnly = true)
    public List<ParkClosureDay> findByRange(LocalDate from, LocalDate to) {
        return repository.findByClosureDateBetween(from, to);
    }

    @Transactional(readOnly = true)
    public boolean isClosedDay(LocalDate date) {
        return repository.existsByClosureDate(date);
    }

    @Transactional
    public ParkClosureDay save(ParkClosureDay closureDay) {
        validateMinimumAdvance(closureDay.getClosureDate());
        if (repository.existsByClosureDate(closureDay.getClosureDate())) {
            throw new BadRequestException("error.closure.already.exists");
        }
        return repository.save(closureDay);
    }

    @Transactional
    public void delete(Long id) {
        ParkClosureDay closureDay = findById(id);
        validateMinimumAdvance(closureDay.getClosureDate());
        repository.delete(closureDay);
    }

    private void validateMinimumAdvance(LocalDate closureDate) {
        LocalDate minimumDate = LocalDate.now().plusMonths(2);
        if (closureDate.isBefore(minimumDate)) {
            throw new InvalidOperationException("error.closure.too.soon");
        }
    }
}

