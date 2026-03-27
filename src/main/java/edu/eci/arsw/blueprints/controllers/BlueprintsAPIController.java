package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /api/v1/blueprints
    @GetMapping
    @Operation(summary = "Obtener todos los blueprints")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa")
    })
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "blueprints retrieved", services.getAllBlueprints());
        return ResponseEntity.ok(response);
    }

    // GET /api/v1/blueprints/{author}
    @GetMapping("/{author}")
    @Operation(summary = "Obtener blueprints por autor")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor sin blueprints")
    })
    public ResponseEntity<?> byAuthor(@PathVariable String author) {
        try {
            ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "author blueprints retrieved", services.getBlueprintsByAuthor(author));
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    @Operation(summary = "Obtener un blueprint por autor y nombre")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Consulta exitosa"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    public ResponseEntity<?> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            ApiResponse<Blueprint> response = new ApiResponse<>(200, "blueprint retrieved", services.getBlueprint(author, bpname));
            return ResponseEntity.ok(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    // POST /api/v1/blueprints
    @PostMapping
    @Operation(summary = "Crear un blueprint")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    public ResponseEntity<?> add(@RequestBody NewBlueprintRequest req) {
        try {
            if (req == null || isBlank(req.author()) || isBlank(req.name()) || req.points() == null) {
                ApiResponse<Void> response = new ApiResponse<>(400, "invalid request data", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<Blueprint> response = new ApiResponse<>(201, "blueprint created", bp);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (BlueprintPersistenceException e) {
            ApiResponse<Void> response = new ApiResponse<>(400, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    @Operation(summary = "Agregar un punto a un blueprint")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Actualización aceptada"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
    })
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            if (p == null) {
                ApiResponse<Void> response = new ApiResponse<>(400, "point payload is required", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<Point> response = new ApiResponse<>(202, "point added", p);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedBody() {
        ApiResponse<Void> response = new ApiResponse<>(400, "invalid request body", null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    public record NewBlueprintRequest(
            String author,
            String name,
            java.util.List<Point> points
    ) { }

    public record ApiResponse<T>(int code, String message, T data) {
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
