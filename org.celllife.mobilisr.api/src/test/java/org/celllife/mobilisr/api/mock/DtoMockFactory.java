package org.celllife.mobilisr.api.mock;

public class DtoMockFactory extends BaseMockFactory {
	
	public static final int MODE_GET = 0;
	public static final int MODE_POST = 1;
	
	private static DtoMockFactory instance;
	
	public static DtoMockFactory _(){
		if (instance == null){
			instance = new DtoMockFactory();
		}
		return instance;
	}
	
	private DtoMockFactory(){
		registerFactories();
	}

	@Override
	protected void registerFactories() {
		register(new MockContactDtoFactory());
		register(new MockMessageStatusDtoFactory());
		register(new MockCampaignDtoFactory());
		register(new MockMessageDtoFactory());
		register(new MockErrorDtoFactory());
		register(new MockSmsMtFactory());
		register(new MockSmsMoFactory());
	}
}
