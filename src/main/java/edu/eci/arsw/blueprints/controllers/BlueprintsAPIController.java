package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "blueprints retrieved", services.getAllBlueprints());
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/blueprints/{author}
    @GetMapping("/{author}")
    public ResponseEntity<?> byAuthor(@PathVariable String author) {
        try {
            ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "author blueprints retrieved", services.getBlueprintsByAuthor(author));
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            ApiResponse<Blueprint> response = new ApiResponse<>(200, "blueprint retrieved", services.getBlueprint(author, bpname));
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/v1/blueprints
    @PostMapping
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<Blueprint> response = new ApiResponse<>(201, "blueprint created", bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<Point> response = new ApiResponse<>(202, "point added", p);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }

    public record ApiResponse<T>(int code, String message, T data) {
    }
}
