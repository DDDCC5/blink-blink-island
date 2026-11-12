# Fully visible left panel

User accepted voice/flight and requests no language control after training, and all panel copy/actions visible without dragging. Preserve accepted spatial center/heading, voice and animation.

Language appears only WELCOME/HOME. Fixed1100x1120dp panel,32sp body with42sp line height, compact12dp spacing. Header/footer remain visible; content is arranged to fit, no vertical scroll or lazy scrolling. Home uses grouped actions, Settings uses inline controls, training keeps concise state message and action rows. No hidden buttons or font reduction.

History is finite pages of2 complete records, previous/next and page indicator, all records reachable rather than old40-item truncation. Pagination stays local UI state and clamps after deletion. Safety sentence and Home stay in footer; save retry stays reachable in one status row.

Verify pagination no missing/duplicate records and clamping, compile/test/style, runtime app launch, wearer confirms all text/actions visible in Chinese and English and language absent on summary.
