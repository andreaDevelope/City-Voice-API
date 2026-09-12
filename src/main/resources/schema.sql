-- Indici unici parziali per Reaction: sostituiscono il vincolo UNIQUE standard,
-- Dettagli: docs/00-stack-and-architecture.md


CREATE UNIQUE INDEX IF NOT EXISTS uq_reaction_on_story
    ON reactions (user_rome_id, story_id)
    WHERE comment_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_reaction_on_comment
    ON reactions (user_rome_id, comment_id)
    WHERE story_id IS NULL;