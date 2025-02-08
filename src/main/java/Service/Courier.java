package Service;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class Courier {
    @NonNull private String login;
    @NonNull private String password;
    private String firstName;
}