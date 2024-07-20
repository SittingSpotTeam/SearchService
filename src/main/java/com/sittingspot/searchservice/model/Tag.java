package com.sittingspot.searchservice.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record Tag(String key, String value) {
}
