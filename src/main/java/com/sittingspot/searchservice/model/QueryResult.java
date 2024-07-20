package com.sittingspot.searchservice.model;

import java.util.UUID;

public record QueryResult(UUID spotId, Location location) {
}
