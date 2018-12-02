package pfrommer.necro.game;

public interface EventProducer {
	public void addListener(EventListener l);
	public void removeListener(EventListener l);
}
