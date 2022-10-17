package scc.srv;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

public class MainApplication extends Application
{
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> resources = new HashSet<Class<?>>();

	public MainApplication() {
		resources.add(ControlResource.class);
		resources.add(MediaResource.class);
		resources.add(UsersResource.class);
		resources.add(AuctionResource.class);
		resources.add(BidResource.class);
		resources.add(QuestionResource.class);

		singletons.add(new MediaResource());
		singletons.add(new UsersResource());
		singletons.add(new AuctionResource());
		singletons.add(new BidResource());
		singletons.add(new QuestionResource());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return resources;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
