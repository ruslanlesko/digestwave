package com.leskor.sanitizer.entities;

public record Paragraph(String content, String style) {
    public Paragraph trimmed() {
        return new Paragraph("code".equals(style) ? content : content.trim(), style);
    }
}
