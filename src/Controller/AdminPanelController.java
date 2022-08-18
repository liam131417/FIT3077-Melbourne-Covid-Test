package Controller;

import Model.BookAndTest.Booking;
import Model.BookAndTest.BookingManager;
import RestAPI.AdminPanelWeb;
import RestAPI.OnsiteBookWeb;
import Model.TestingSite.TestingSite;
import View.View;

import java.util.ArrayList;

public class AdminPanelController {
    private static AdminPanelController instance;
    private AdminPanelController(){}

    public static AdminPanelController getInstance(){
        if(instance == null){
            instance = new AdminPanelController();
        }
        return instance;
    }

    public void subMenu(View view) throws Exception {
        view.printPrompt("1: Modify Booking");
        view.printPrompt("2: View all Booking");
        view.printPrompt("3: Delete Booking");
        view.printPrompt("4: Notification");
        view.printPrompt("5: Cancel Booking");
        int choice = view.inputInt();

        switch (choice){
            case 1:{
                modifyBooking(view);
            }break;
            case 2:{
                viewBooking(view);
            }break;
            case 3:{
                deleteBooking(view);
            }break;
            case 4:{
                notifyBooking(view);
            }break;
            case 5:{
                cancelBooking(view);
            }
        }
    }
    public void cancelBooking(View view) throws Exception {
        OnsiteBookController.getInstance().modifyByBookingID(view,2);
    }
    public void notifyBooking(View view) throws Exception {
        TestingSite workingLocation = AdminPanelWeb.getInstance().getCurrentUserWorkingLocation();
        view.printPrompt("You work in "+workingLocation.getName());
        BookingManager bm = OnsiteBookWeb.getInstance().loadBooking();
        ArrayList<Booking> bookings = bm.getBookings();
        for (int i = 0 ; i < bookings.size();i++){
            if(bookings.get(i).getTest()!=null && bookings.get(i).getTest().getStatus().equals("CANCELLED")
                    && bookings.get(i).getTestingSite().getId().equals(workingLocation.getId())
                        ){
                view.printPrompt(bookings.get(i).toString());
            }
        }
        subMenu(view);
    }

    public void deleteBooking(View view) throws Exception {
        view.printPrompt("Enter Booking id to Delete");
        String id = view.inputString();
        Booking book = AdminPanelWeb.getInstance().getBooking(id);
        view.printPrompt(book.toString());
        view.printPrompt("");
        view.printPrompt("Press 1 to confirm Delete");
        int choice = view.inputInt();
        if (choice==1) {
            AdminPanelWeb.getInstance().deleteBooking(book);
        }
        subMenu(view);
    }

    public void modifyBooking(View view) throws Exception {
        OnsiteBookController.getInstance().modifyByBookingID(view,1);
    }
    public void viewBooking(View view) throws Exception {
        BookingManager bm = OnsiteBookWeb.getInstance().loadBooking();
        bm.viewBooking(view);
        subMenu(view);

    }


}
