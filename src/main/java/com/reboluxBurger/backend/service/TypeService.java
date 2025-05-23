package com.reboluxBurger.backend.service;

import com.reboluxBurger.backend.entity.Type;
import com.reboluxBurger.backend.repository.TypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TypeService {

    private final TypeRepository typeRepository;

    public TypeService(TypeRepository typeRepository) {
        this.typeRepository = typeRepository;
    }

    // Devuelve todos los tipos
    public List<Type> getAllTypes() {
        return typeRepository.findAll();
    }

    // Crea un nuevo tipo si no existe otro con ese nombre
    public Type createType(Type type) {
        if (typeRepository.existsByName(type.getName())) {
            throw new RuntimeException("Ya existe un tipo con ese nombre");
        }
        return typeRepository.save(type);
    }

    // Borra un tipo por id si existe
    public void deleteType(Long id) {
        if (!typeRepository.existsById(id)) {
            throw new RuntimeException("tipo no encontrado");
        }
        typeRepository.deleteById(id);
    }

    // Obtiene un tipo por nombre, o lo crea si no existe
    public Type getOrCreateType(String name) {
        String normalized = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        Optional<Type> existing = typeRepository.findByName(normalized);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            return typeRepository.save(new Type(null, normalized));
        }
    }


}
