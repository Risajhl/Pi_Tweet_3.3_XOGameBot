import ir.pi.project.server.controller.ResponseSender;
import ir.pi.project.shared.event.Event;
import ir.pi.project.shared.response.Response;

import java.util.ArrayList;
import java.util.List;

public class BotSender implements ResponseSender {
    private final List<Event> events=new ArrayList<>();

    public void addEvent(Event event){

        synchronized (events) {
            events.add(event);
            events.notifyAll();
        }
    }

    @Override
    public Event getEvent() {

        synchronized (events) {
            while (events.isEmpty()) {
                try {
                    events.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return events.remove(events.size()-1);
        }
    }

    @Override
    public void sendResponse(Response response) {
    }

    @Override
    public void close() {

    }
}
