package it.cityvoice.api.shared.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int size, boolean hasNext) {}
