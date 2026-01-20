package requests.Skeleton.Interface;

import io.restassured.response.ValidatableResponse;
import models.BaseModel;

public interface CrudMethods {
    Object post(BaseModel model);
    Object get();
    Object update(long id, BaseModel model);
    Object delete(long id);
    Object put(BaseModel model);
}
