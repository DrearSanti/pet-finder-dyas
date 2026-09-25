---
paths:
  - "src/test/**"
---

# Pruebas

- Sufijo por nivel: `*Test` unitarias (Surefire, `./mvnw test`), `*IT` integración y sistema (Failsafe, `./mvnw verify`), `*UIT` interfaz (perfil `ui`).
- Estructura `// Arrange`, `// Act`, `// Assert` y `@DisplayName` en español que diga el comportamiento.
- Nada compartido entre pruebas; cada una crea sus datos.
- Nunca afirmes un ID fijo (`PF-001`) ni un tamaño absoluto de lista en integración o sistema: la base puede traer datos de otras pruebas.
- Toda `@SpringBootTest` lleva `ANTHROPIC_API_KEY=` en `properties` (y `petfinder.datos-ejemplo=false` si cuenta reportes). Ninguna prueba llama a Claude.
- Mockito: `@Mock` + `@InjectMocks` en unitarias; `@MockitoBean` en `@WebMvcTest`. Verifica orden con `InOrder` cuando el orden es la regla.
- Integración con H2 real: `@DataJpaTest` para el adaptador, `@SpringBootTest` + `@Transactional` para caso de uso + base.
- Sistema: `@SpringBootTest(webEnvironment = RANDOM_PORT)` y solo HTTP; nada de beans internos.
- UI: Page Objects, `WebDriverWait`, selectores `[data-prueba=...]`. Nunca `Thread.sleep` ni `By.xpath`.
- Si una prueba falla, arregla el código, no la prueba.
