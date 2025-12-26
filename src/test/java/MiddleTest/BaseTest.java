package MiddleTest;

import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import requests.ProfileDataRequester;
import specs.RequestSpecs;

public abstract class BaseTest {
    protected SoftAssertions softly;


    // креды (можно потом вынести в env / properties)
    protected final String username1 = "Bogdan2002";
    protected final String password1 = "Bogdanio_%12345q";

    protected final String username2 = "Bogdan_2002";
    protected final String password2 = "Bogdanio_%12345q";

    // auth specs
    protected RequestSpecification authSpecUser1;
    protected RequestSpecification authSpecUser2;

    // common requesters
    protected ProfileDataRequester user1Profile;
    protected ProfileDataRequester user2Profile;

    @BeforeEach
    void baseSetUp() {
        softly = new SoftAssertions();
        authSpecUser1 = RequestSpecs.authUser(username1, password1);
        authSpecUser2 = RequestSpecs.authUser(username2, password2);

        user1Profile = new ProfileDataRequester(authSpecUser1);
        user2Profile = new ProfileDataRequester(authSpecUser2);
    }
}
