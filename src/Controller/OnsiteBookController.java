package Controller;

import Model.BookAndTest.*;
import RestAPI.TestingSiteWeb;
import Model.TestingSite.TestingSite;
import Model.TestingSite.TestingSiteManager;
import Model.User.Customer;
import Model.User.UserManager;
import RestAPI.OnsiteBookWeb;
import View.View;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * OnsiteBookController class represents the controller to perform onsite booking
 */
public class OnsiteBookController {
    /**
     * instance of OnsiteBookController
     */
    private static OnsiteBookController instance;
    private OnsiteBookController(){}

    /**
     * getInstance() method
     * @return instance of OnsiteBookController
     */
    public static OnsiteBookController getInstance(){
        if(instance==null){
            instance = new OnsiteBookController();
        }
        return instance;
    }

    /**
     * subMenu() method
     * lets user choose between 2 actions
     */
    public void subMenu(View view) throws Exception {
        view.printPrompt("Choose the action:");
        view.printPrompt("1: Place Booking");
        view.printPrompt("2: Check Booking");
        view.printPrompt("3: Modify Booking by Phone");
        int choice = view.inputInt();
        switch(choice){
            case 1:{
                placeBooking(view);
            }break;
            case 2:{
                checkBooking(view);
            }break;
            case 3:{
                modifyBookingbyPhone(view);
            }
            default:
                subMenu(view);
        }
    }

    /**
     * placebooking() method
     * allows user to place booking
     */
    public void placeBooking(View view) throws Exception{
        String custID;
        String testSiteId;
        Customer customer;
        TestingSite testingSite;
        do {
            view.printPrompt("Please Enter Customer ID");
            custID = view.inputString();
            customer = OnsiteBookWeb.getInstance().validateCust(custID);
            if (customer == null) {
                view.printPrompt("Please enter valid CustomerID");
            }
        }while(customer == null);
        do {
            view.printPrompt("Please Enter Testing ID");
            testSiteId = view.inputString();
            testingSite = OnsiteBookWeb.getInstance().validateTestSite(testSiteId);
            if (testingSite == null){
                view.printPrompt("Please enter valid TestSite ID");
            }
        }while(testingSite == null);
        view.printPrompt("Please Enter Notes");
        String notes = view.inputString();
        Booking postBooking = new Booking(customer,testingSite,notes);
        Booking completeBooking = OnsiteBookWeb.getInstance().uploadDetail(postBooking);

        int testChoice = 0;
        view.printPrompt("Select Test");
        view.printPrompt("1: PCR");
        view.printPrompt("2: RAT");
        testChoice = view.inputInt();
        CovidTest ct = null;
        view.printPrompt("Enter notes for the Covid test");
        String testNotes = view.inputString();
        if (testChoice == 1 ){
            ct = new PCRTest(customer, UserManager.getInstance().getCurrentUser(),
                    "PENDING","PENDING",testNotes);
        }
        else if(testChoice ==2 ){
            ct = new RATTest(customer,UserManager.getInstance().getCurrentUser(),
                    "PENDING","PENDING",testNotes);
        }
        completeBooking.setTest(ct);
        OnsiteBookWeb.getInstance().uploadTest(completeBooking);
        view.printPrompt("Booking Pin:"+completeBooking.getSmsPin());
        subMenu(view);
    }

    /**
     * checkBooking() method
     * allows user to check their booking
     */
    public Booking checkBooking(View view) throws Exception {
        BookingManager bm = OnsiteBookWeb.getInstance().loadBooking();
        Booking pin = null;
        do {
            view.printPrompt("Enter Your SMS Pin code");
            int smsPin = view.inputInt();
            pin = bm.findPin(smsPin);
            if (pin != null) {
                view.printPrompt(pin.toString());
            }
        }while(pin == null);
        return pin;
    }

    public void subCustMenu(View view) throws Exception {
        view.printPrompt("1: Modify Booking");
        view.printPrompt("2: Cancel Booking");
        int choice = view.inputInt();
        modifyBookingMenu(view,choice);
    }
    public void modifyBookingMenu(View view,int sel) throws Exception {
        view.printPrompt("1: Check current booking");
        view.printPrompt("2: Enter Booking id");
        int choice = view.inputInt();

        switch (choice){
            case 1:{
                modifyBooking(OnsiteBookWeb.getInstance().userBooking(UserManager.getInstance().getCurrentUser().getId(),null),view,sel);
            } break;
            case 2:{
                modifyByBookingID(view,sel);
            }
        }
    }
    public void modifyByBookingID(View view,int sel) throws Exception {
        view.printPrompt("Enter Booking id");
        String bookingid = view.inputString();
        modifyBooking(OnsiteBookWeb.getInstance().userBooking(null,bookingid),view,sel);
    }
    public void modifyBooking(Booking book,View view,int sel) throws Exception {
        if (sel == 1){
            view.printPrompt("Select to modify:");
            view.printPrompt("1: Date of Perform: " + book.getTest().getDatePerformed());
            view.printPrompt("2: Testing Site: " + book.getTestingSite().getName());
            int choice = view.inputInt();
            switch(choice){
                case 1:{
                    changeDate(book,view);
                }break;
                case 2:{
                    changeTestingSite(book,view);
                }break;
                default:{

                }
        }
        }
        else if (sel == 2){
            view.printPrompt("1: Confirm to cancel");
            view.printPrompt("2: Return to home");
            int choice = view.inputInt();
            switch(choice){
                case 1:
                    OnsiteBookWeb.getInstance().cancelBooking(book);
            }
        }
    }
    public void changeDate(Booking book,View view) throws Exception {
        Instant instant = Instant.parse(book.getTest().getDatePerformed());
        if (instant.isBefore(Instant.now())){
            view.printPrompt("This is a lapsed Booking");
            subCustMenu(view);
        }
        if (book.getTest().getStatus().equals("CANCELLED")){
            view.printPrompt("This is a canceled Booking");
            subCustMenu(view);
        }
        view.printPrompt("Change Date to:");
        view.printPrompt("1: "+instant.plus(1, ChronoUnit.DAYS));
        view.printPrompt("2: "+instant.plus(2, ChronoUnit.DAYS));
        view.printPrompt("3: "+instant.plus(3, ChronoUnit.DAYS));
        int days = view.inputInt();
        book.getTest().setDatePerformed(instant.plus(days,ChronoUnit.DAYS).toString());
        OnsiteBookWeb.getInstance().modifyDate(book);

    }

    public void changeTestingSite(Booking book, View view) throws Exception {
        if(TestingSiteManager.getInstance().getTestingSites().size()==0){
            TestingSiteWeb.getInstance().loadList();
        }
        ArrayList<TestingSite> ts = TestingSiteManager.getInstance().getTestingSites();

        view.printPrompt("Select Testing Site");
        for(int i = 0; i< ts.size();i++){
            view.printPrompt(i+" : "+ ts.get(i).getName());
        }
        int choice = view.inputInt();

        book.setTestingSite(ts.get(choice));

        OnsiteBookWeb.getInstance().modifyTestSite(book);
    }


    public void modifyBookingbyPhone(View view) throws Exception{
        view.printPrompt("Modify by:");
        view.printPrompt("1: Booking Pin");
        view.printPrompt("2: Booking Id");
        int choice = view.inputInt();
        Booking book = null;
        switch (choice){
            case 1:{
                 book = checkBooking(view);
                 modifyBooking(book,view,1);
            }break;
            case 2:
                modifyByBookingID(view,1);
    }
}

}