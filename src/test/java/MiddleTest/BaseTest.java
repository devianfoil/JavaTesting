package MiddleTest;

import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import requests.Skeleton.ProfileDataHelper;
import specs.RequestSpecs;

public abstract class BaseTest {

    // auth
    protected RequestSpecification authSpecUser1;
    protected RequestSpecification authSpecUser2;
    protected SoftAssertions softly;

    // credentials
    protected final String username1 = "Bogdan2002";
    protected final String password1 = "Bogdanio_%12345q";

    protected final String username2 = "Bogdan_2002";
    protected final String password2 = "Bogdanio_%12345q";



    // dynamic test state
    protected int user1AccountId;
    protected int user2AccountId;

    @BeforeEach
    void baseSetUp() {
        softly = new SoftAssertions();

        authSpecUser1 = RequestSpecs.authUser(username1, password1);
        authSpecUser2 = RequestSpecs.authUser(username2, password2);

    }
}

