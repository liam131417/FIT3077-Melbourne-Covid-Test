package RestAPI;


import Model.BookAndTest.Booking;
import Model.TestingSite.TestingSite;
import Model.User.UserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;

public class AdminPanelWeb extends RESTApi{
    private static AdminPanelWeb instance;
    private AdminPanelWeb(){}
    public static AdminPanelWeb getInstance(){
        if(instance == null) {
            instance = new AdminPanelWeb();
        }
        return instance;
    }
    public Booking getBooking(String id) throws Exception{
        String bookurl = "https://fit3077.com/api/v2/booking/"+id+"?fields=covidTests";
        HttpResponse<String> response = get(bookurl);
        ObjectNode node = new ObjectMapper().readValue(response.body(),ObjectNode.class);
        Booking book = createBooking(node);
        return book;
    }
    public void deleteBooking(Booking book) throws Exception {
        String bookurl = "https://fit3077.com/api/v2/booking/"+book.getBookingId();
        String testurl = "https://fit3077.com/api/v2/covid-test/"+book.getTest().getId();
        delete(testurl);
        delete(bookurl);
    }

    public TestingSite getCurrentUserWorkingLocation() throws Exception{
        String userId = UserManager.getInstance().getCurrentUser().getId();
        String url =  "https://fit3077.com/api/v2/user/"+ userId +"?fields=testsAdministered";

        HttpResponse<String> response = get(url);

        ObjectNode userNode = new ObjectMapper().readValue(response.body(),ObjectNode.class);


        return createTestingSite((ObjectNode) userNode.get("testsAdministered").get(0).get("booking").get("testingSite"));
    }
}
