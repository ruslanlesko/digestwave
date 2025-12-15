package com.leskor.digestwave.web;

import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.service.MentionService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/mentions")
public class MentionController {
    private final MentionService mentionService;

    MentionController(MentionService mentionService) {
        this.mentionService = mentionService;
    }

    /**
     * Fetch mentions for a given keyword.
     *
     * <p> Mentions are grouped by publication date. Example payload:
     *
     * <pre>
     *     [
     *      {
     *          "publishedAt": "2023-10-01",
     *          "count": 2,
     *          "instances": [
     *              {
     *                  "articleUrl": "https://example.com/article1",
     *                  "sentiment": "POSITIVE"
     *              },
     *              {
     *                  "articleUrl": "https://example.com/article2",
     *                  "sentiment": "NEUTRAL"
     *              }
     *          ]
     *      }
     *     ]
     *
     * @param keyword keyword to search for
     * @return list of mentions
     */
    @GetMapping(value = "/{keyword}", produces = "application/json")
    public List<MentionPayload> getMentions(
            @PathVariable String keyword,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        LocalDate start = parseDate(startDate, "startDate");
        LocalDate end = parseDate(endDate, "endDate");

        if (start != null && end != null && start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be on or before endDate");
        }

        return mentionService.fetchMentions(keyword, start, end);
    }

    private static LocalDate parseDate(String candidate, String fieldName) {
        if (candidate == null || candidate.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(candidate, MentionService.DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must match yyyy-MM-dd");
        }
    }
}
