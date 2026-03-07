package com.magicworld.tfg_angular_springboot.statistics.service;

import com.magicworld.tfg_angular_springboot.employee.EmployeeRole;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

/**
 * Realistic hourly base rates (EUR) by employee role in a theme park.
 * Sources: average wages for theme park roles in Spain/Europe.
 *
 * Overtime is paid at 1.5x the base rate (legal standard in Spain).
 */
public final class SalaryRateConfig {

    private static final BigDecimal OVERTIME_MULTIPLIER = new BigDecimal("1.50");

    private static final Map<EmployeeRole, BigDecimal> HOURLY_RATES = new EnumMap<>(EmployeeRole.class);

    static {
        HOURLY_RATES.put(EmployeeRole.OPERATOR, new BigDecimal("12.00"));
        HOURLY_RATES.put(EmployeeRole.SECURITY, new BigDecimal("13.00"));
        HOURLY_RATES.put(EmployeeRole.MEDICAL, new BigDecimal("18.00"));
        HOURLY_RATES.put(EmployeeRole.MAINTENANCE, new BigDecimal("11.00"));
        HOURLY_RATES.put(EmployeeRole.GUEST_SERVICES, new BigDecimal("10.00"));
    }

    private SalaryRateConfig() {}

    public static BigDecimal getHourlyRate(EmployeeRole role) {
        return HOURLY_RATES.getOrDefault(role, new BigDecimal("10.00"));
    }

    public static BigDecimal getOvertimeRate(EmployeeRole role) {
        return getHourlyRate(role).multiply(OVERTIME_MULTIPLIER);
    }

    public static BigDecimal getOvertimeMultiplier() {
        return OVERTIME_MULTIPLIER;
    }
}

