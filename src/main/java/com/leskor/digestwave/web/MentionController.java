package com.leskor.digestwave.web;

import com.leskor.digestwave.model.MentionPayload;
import com.leskor.digestwave.service.MentionService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mentions")
public class MentionController {
    private final MentionService mentionService;

    @Autowired
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
    public List<MentionPayload> getMentions(@PathVariable String keyword) {
        return mentionService.fetchMentions(keyword);
    }
}
