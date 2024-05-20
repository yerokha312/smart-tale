package dev.yerokha.smarttale.controller.websocket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Web Socket API", description = "Documentation for connecting to Web sockets")
@Controller
public class WebSocketController {

    @Operation(
            summary = "WebSocket Documentation",
            description = "Detailed documentation of WebSocket message formats and channels",
            tags = {"websocket"}
    )
    @GetMapping("/ws-documentation")
    public String wsDocumentation() {
        return "ws-documentation";
    }
}

