package com.magicworld.tfg_angular_springboot;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Epic("Aplicación MagicWorld")
@Feature("Contexto de Aplicación")
class MagicWorldApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@Story("Carga de Contexto")
	@Description("Verifica que el contexto de Spring se carga correctamente")
	@Severity(SeverityLevel.BLOCKER)
	@DisplayName("El contexto de Spring se carga correctamente")
	void contextLoads() {
		assertNotNull(applicationContext, "El contexto de aplicación debe cargarse correctamente");
	}

}
