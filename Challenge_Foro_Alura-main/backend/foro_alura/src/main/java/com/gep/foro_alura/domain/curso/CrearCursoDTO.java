package com.gep.foro_alura.domain.curso;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CrearCursoDTO(@NotBlank String name, @NotNull Categoria categoria) {
}
