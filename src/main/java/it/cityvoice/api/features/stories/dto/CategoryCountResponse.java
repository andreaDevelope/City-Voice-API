package it.cityvoice.api.features.stories.dto;

import it.cityvoice.api.features.stories.enums.ReportCategory;

public record CategoryCountResponse(ReportCategory category, long count) {}
