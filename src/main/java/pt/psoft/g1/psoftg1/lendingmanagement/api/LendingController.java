package pt.psoft.g1.psoftg1.lendingmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.services.CreateLendingRequest;
import pt.psoft.g1.psoftg1.lendingmanagement.services.LendingService;
import pt.psoft.g1.psoftg1.lendingmanagement.services.SetLendingReturnedRequest;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.services.ReaderService;
import pt.psoft.g1.psoftg1.shared.api.ListResponse;
import pt.psoft.g1.psoftg1.shared.services.ConcurrencyService;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.Librarian;
import pt.psoft.g1.psoftg1.usermanagement.model.User;
import pt.psoft.g1.psoftg1.usermanagement.services.UserService;

import java.util.List;
import java.util.Objects;

@Tag(name = "Lendings", description = "Endpoints for managing Lendings")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/lendings")
public class LendingController {

    private static final String IF_MATCH = "If-Match";

    private final LendingService lendingService;
    private final ReaderService readerService;
    private final UserService userService;
    private final ConcurrencyService concurrencyService;

    private final LendingViewMapper lendingViewMapper;

    @Operation(summary = "Creates a new Lending")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LendingView> create(@Valid @RequestBody final CreateLendingRequest resource) {

        final var lending = lendingService.create(resource);

        final var newlendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .pathSegment(lending.getLendingNumber())
                .build().toUri();

        return ResponseEntity.created(newlendingUri)
                .eTag(Long.toString(lending.getVersion()))
                .body(lendingViewMapper.toLendingView(lending));
    }

    @Operation(summary = "Gets a specific Lending")
    @GetMapping(value = "/{year}/{seq}")
    public ResponseEntity<LendingView> findByLendingNumber(
            Authentication authentication,
            @PathVariable("year")
                @Parameter(description = "The year of the Lending to find")
                final Integer year,
            @PathVariable("seq")
                @Parameter(description = "The sequencial of the Lending to find")
                final Integer seq) {

        String ln = year + "/" + seq;
        final var lending = lendingService.findByLendingNumber(ln)
                .orElseThrow(() -> new NotFoundException(Lending.class, ln));

        User loggedUser = userService.getAuthenticatedUser(authentication);

        //if Librarian is logged in, skip ahead
        if (!(loggedUser instanceof Librarian)) {
            final var loggedReaderDetails = readerService.findByUsername(loggedUser.getUsername())
                    .orElseThrow(() -> new NotFoundException(ReaderDetails.class, loggedUser.getUsername()));

            //if logged Reader matches the one associated with the lending, skip ahead
            if (!Objects.equals(loggedReaderDetails.getReaderNumber(), lending.getReaderDetails().getReaderNumber())) {
                throw new AccessDeniedException("Reader does not have permission to view this lending");
            }
        }
        final var lendingUri = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .build().toUri();

        return ResponseEntity.ok().location(lendingUri)
                .eTag(Long.toString(lending.getVersion()))
                .body(lendingViewMapper.toLendingView(lending));
    }

    @Operation(summary = "Sets a lending as returned")
    @PatchMapping(value = "/{year}/{seq}")
    public ResponseEntity<LendingView> setLendingReturned(
            final WebRequest request,
            final Authentication authentication,
            @PathVariable("year")
                @Parameter(description = "The year component of the Lending to find")
                final Integer year,
            @PathVariable("seq")
                @Parameter(description = "The sequential component of the Lending to find")
                final Integer seq,
            @Valid @RequestBody final SetLendingReturnedRequest resource) {
        final String ifMatchValue = request.getHeader(IF_MATCH);
        if (ifMatchValue == null || ifMatchValue.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You must issue a conditional PATCH using 'if-match'");
        }
        String ln = year + "/" + seq;
        final var maybeLending = lendingService.findByLendingNumber(ln)
                .orElseThrow(() -> new NotFoundException(Lending.class, ln));

        User loggedUser = userService.getAuthenticatedUser(authentication);

        final var loggedReaderDetails = readerService.findByUsername(loggedUser.getUsername())
                .orElseThrow(() -> new NotFoundException(ReaderDetails.class, loggedUser.getUsername()));

        //if logged Reader matches the one associated with the lending, skip ahead
        if (!Objects.equals(loggedReaderDetails.getReaderNumber(), maybeLending.getReaderDetails().getReaderNumber())) {
            throw new AccessDeniedException("Reader does not have permission to edit this lending");
        }

        final var lending = lendingService.setReturned(ln, resource, concurrencyService.getVersionFromIfMatchHeader(ifMatchValue));

        return ResponseEntity.ok()
                .eTag(Long.toString(lending.getVersion()))
                .body(lendingViewMapper.toLendingView(lending));
    }

    @Operation(summary = "Get average lending duration")
    @GetMapping(value = "/avgDuration")
    public @ResponseBody String getAvgDuration() {
        return lendingService.getAverageDuration();
    }

    @Operation(summary = "Get list of overdue lendings")
    @GetMapping(value = "/overdue")
    public ListResponse<LendingView> getOverdueLendings(@RequestBody final Page page) {
        final List<Lending> overdueLendings = lendingService.getOverdue(page);
        return new ListResponse<>(lendingViewMapper.toLendingView(overdueLendings));
    }

}
