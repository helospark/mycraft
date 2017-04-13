package com.helospark.mycraft.mycraft.game;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.helospark.mycraft.mycraft.actor.Actor;
import com.helospark.mycraft.mycraft.actor.ActorKeyboardControllerComponent;
import com.helospark.mycraft.mycraft.actor.ActorMessageControllerComponent;
import com.helospark.mycraft.mycraft.actor.BlockChangerComponent;
import com.helospark.mycraft.mycraft.actor.HealthComponent;
import com.helospark.mycraft.mycraft.actor.HittableActorComponent;
import com.helospark.mycraft.mycraft.actor.InventoryComponent;
import com.helospark.mycraft.mycraft.actor.InventoryItem;
import com.helospark.mycraft.mycraft.actor.MyIdGenerator;
import com.helospark.mycraft.mycraft.actor.PhysicsComponent;
import com.helospark.mycraft.mycraft.actor.ToolHoldingComponent;
import com.helospark.mycraft.mycraft.actor.TransformComponent;
import com.helospark.mycraft.mycraft.blockupdaters.BlockUpdateManager;
import com.helospark.mycraft.mycraft.md5loader.Md5Model;
import com.helospark.mycraft.mycraft.messages.ActorLifeMessage;
import com.helospark.mycraft.mycraft.messages.UserConnectionResultMessage;
import com.helospark.mycraft.mycraft.pathsearch.PathSearchService;
import com.helospark.mycraft.mycraft.render.HudRenderer;
import com.helospark.mycraft.mycraft.render.MyTextureLoader;
import com.helospark.mycraft.mycraft.render.RenderableModelNode;
import com.helospark.mycraft.mycraft.render.RenderableNode;
import com.helospark.mycraft.mycraft.services.ActorSearchService;
import com.helospark.mycraft.mycraft.services.ClientService;
import com.helospark.mycraft.mycraft.services.GlobalParameters;
import com.helospark.mycraft.mycraft.services.PhysicsResolverService;
import com.helospark.mycraft.mycraft.services.ServerService;
import com.helospark.mycraft.mycraft.services.SpriteWriterService;
import com.helospark.mycraft.mycraft.singleton.Singleton;
import com.helospark.mycraft.mycraft.views.CraftingMenu;
import com.helospark.mycraft.mycraft.views.HandItemView;
import com.helospark.mycraft.mycraft.views.InventoryView;
import com.helospark.mycraft.mycraft.views.LifeShowingView;
import com.helospark.mycraft.mycraft.views.ViewStack;
import com.helospark.mycraft.mycraft.window.Input;
import com.helospark.mycraft.mycraft.window.Message;
import com.helospark.mycraft.mycraft.window.MessageHandler;
import com.helospark.mycraft.mycraft.window.MessageListener;
import com.helospark.mycraft.mycraft.window.MessageTypes;
import com.helospark.mycraft.mycraft.window.Window;
import com.helospark.mycraft.mycraft.xml.ModelLoader;
import com.helospark.mycraft.mycraft.xml.XmlParser;

public class Game implements MessageListener {
	private Camera activeCamera;
	private Window window;
	private MessageHandler messageHandler;
	private boolean running = true;
	private int desiredFps = 60;
	private Actor testActor;
	private AsyncChunkHandler chunkHandler;
	RenderableNode testRender;
	private MyTextureLoader textureLoader;
	private ActorSearchService actorSearchService;
	private Input input;
	RenderableModelNode renderableModelNode;
	MyIdGenerator idGenerator;
	PhysicsResolverService physics;
	SpriteWriterService textWriter;
	HudRenderer hudRenderer;
	ViewStack viewStack;
	XmlParser xmlParser;
	ModelLoader modelLoader;

	long lastFps = 0;
	int frames = 0;
	ServerService serverService;
	ClientService clientService;

	boolean isServer;
	boolean isSinglePlayer;
	volatile boolean initialized = false;

	GameMapRenderer gameMapRenderer;
	GlobalParameters globalParameters;
	Md5Model md5Model = null;
	Actor zombieActor;
	BlockUpdateManager blockUpdateManager;

	boolean isRunnedOnce = false;

	@Autowired
	private PathSearchService pathSearchService;

	public Game() {

	}

	private void initializeNetwork(ApplicationContext context) {
		// Network network;
		// if (isServer) {
		// network = new Network(ServerService.SERVER_PORT);
		// serverService = new ServerService(network);
		// } else {
		// network = new Network();
		// }
		// network.initialize();
		// InetAddress address;
		// try {
		// address = InetAddress.getByName("127.0.0.1");
		// } catch (Exception e) {
		// throw new RuntimeException("Unable to get address");
		// }
		// clientService = context.getBean(ClientService.class);
		// clientService.setNetwork(network);
		// clientService.setServerUser(new NetworkUser(address,
		// ServerService.SERVER_PORT));
		// clientService.setClientAndServerSame(isServer);
		// clientService.initialize();
		// clientService.start();
		// if (!isServer) {
		// Message message = new ConnectToServerMessage(
		// MessageTypes.SERVER_CONNECTION_REQUEST_MESSAGE, "asd", "",
		// 0);
		// messageHandler.sendImmediateMessage(message);
		// }
	}

	public void initialize() {
		xmlParser = new XmlParser();
		xmlParser.parseXml("resources/init.xml");

		ApplicationContext context = Singleton.getInstance().getContext();

		globalParameters = context.getBean(GlobalParameters.class);
		desiredFps = globalParameters.desiredFps;

		messageHandler = context.getBean(MessageHandler.class);
		initializeNetwork(context);
		messageHandler.registerListener(this, MessageTypes.ESCAPE_REQUESTED);

		if (isServer) {
			initializeAfterNetworkConnected();
		} else {
			messageHandler.registerListener(this, MessageTypes.USER_CONNECTION_RESULT_MESSAGE);
		}
	}

	private void initializeAfterNetworkConnected() {
		ApplicationContext context = Singleton.getInstance().getContext();
		idGenerator = context.getBean(MyIdGenerator.class);

		testActor = new Actor(idGenerator.getNextId());
		textureLoader = context.getBean(MyTextureLoader.class);
		physics = context.getBean(PhysicsResolverService.class);
		hudRenderer = context.getBean(HudRenderer.class);
		createCameraWithOrientationIfNotAlreadyExists(new Vector3f(10, 14, 10), new Vector3f(0, 0,
				0));

		window = context.getBean(Window.class);
		window.show();
		xmlParser.parseXml("resources/xmlList.xml");

		viewStack = context.getBean(ViewStack.class);
		modelLoader = context.getBean(ModelLoader.class);

		chunkHandler = context.getBean(AsyncChunkHandler.class);
		chunkHandler.start();
		input = context.getBean(Input.class);
		textWriter = context.getBean(SpriteWriterService.class);
		actorSearchService = context.getBean(ActorSearchService.class);
		pathSearchService = context.getBean(PathSearchService.class);
		actorSearchService.addActor(testActor);
		blockUpdateManager = context.getBean(BlockUpdateManager.class);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_CULL_FACE);

		try {
			Mouse.create();
			Keyboard.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		registerAllViews();
		createActor();

		try {
			Mouse.create();
			Keyboard.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		textWriter.createText("TimesRoman", 50);
		hudRenderer.initialize();
		hudRenderer.registerSpriteTexture(textureLoader.load2DTexture("resources/sprite.png"), 32,
				32);

		gameMapRenderer = context.getBean(GameMapRenderer.class);
		gameMapRenderer.initialize();

		initialized = true;

		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		// messageHandler.sendImmediateMessage(new
		// GenericIntegerVectorMessage(MessageTypes.GROW_TREE,
		// Message.MESSAGE_TARGET_ANYONE, 33, 10, 33));

	}

	private void createActor() {
		InventoryItem inventoryItem = new InventoryItem(8, 1);

		TransformComponent component = new TransformComponent();
		component.setOwner(testActor);
		PhysicsComponent physicsComponent = new PhysicsComponent(new Vector3f(1.0f, 1.5f, 1.0f));
		physicsComponent.setShouldApplyGravity(true);
		physicsComponent.setOwner(testActor);
		physicsComponent.afterInit();
		InventoryComponent inventoryComponent = new InventoryComponent(10, 40, inventoryItem);
		inventoryComponent.setOwner(testActor);
		inventoryComponent.afterInit();

		BlockChangerComponent blockComponent = new BlockChangerComponent();
		blockComponent.setOwner(testActor);
		blockComponent.afterInit();

		ToolHoldingComponent toolHoldingComponent = new ToolHoldingComponent();
		toolHoldingComponent.setOwner(testActor);
		toolHoldingComponent.afterInit();

		ActorMessageControllerComponent actorMessageComponent = new ActorMessageControllerComponent();
		actorMessageComponent.setOwner(testActor);
		actorMessageComponent.afterInit();

		ActorKeyboardControllerComponent actorKeyboardComponent = new ActorKeyboardControllerComponent();
		actorKeyboardComponent.setOwner(testActor);
		actorKeyboardComponent.afterInit();

		HealthComponent healthComponent = new HealthComponent(20);
		healthComponent.setOwner(testActor);
		healthComponent.afterInit();

		HittableActorComponent playerHitActorComponent = new HittableActorComponent();
		playerHitActorComponent.setOwner(testActor);
		playerHitActorComponent.afterInit();

		testActor.addComponent(TransformComponent.TRANSFORM_COMPONENT_NAME, component);
		testActor.addComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME, physicsComponent);
		testActor.addComponent(InventoryComponent.INVENTORY_COMPONENT_NAME, inventoryComponent);
		testActor.addComponent(BlockChangerComponent.BLOCK_CHANGE_COMPONENT_NAME, blockComponent);
		testActor.addComponent(ToolHoldingComponent.TOOL_HOLDING_COMPONENT_NAME,
				toolHoldingComponent);
		testActor.addComponent(ActorMessageControllerComponent.ACTOR_MESSAGE_COMPONENT_NAME,
				actorMessageComponent);
		testActor.addComponent(ActorKeyboardControllerComponent.ACTOR_KEYBOARD_COMPONENT_NAME,
				actorKeyboardComponent);
		testActor.addComponent(HealthComponent.HEALTH_COMPONENT_NAME, healthComponent);
		testActor.addComponent(HittableActorComponent.HITTABLE_ACTOR_COMPONENT_NAME,
				playerHitActorComponent);

		messageHandler.sendImmediateMessage(new ActorLifeMessage(MessageTypes.NEW_ACTOR_MESSAGE,
				Message.MESSAGE_TARGET_ANYONE, testActor));

		for (int i = 0; i < 1; ++i) {
			inventoryComponent.addToInventory(new InventoryItem(6, 1));
		}

		for (int i = 0; i < 1; ++i) {
			inventoryComponent.addToInventory(new InventoryItem(1, 1));
		}
		for (int i = 0; i < 1; ++i) {
			inventoryComponent.addToInventory(new InventoryItem(7, 10));
		}
		inventoryComponent.addToInventory(new InventoryItem(9, 10));
		inventoryComponent.addToInventory(new InventoryItem(10, 10));
		inventoryComponent.addToInventory(new InventoryItem(14, 10));
		inventoryComponent.addToInventory(new InventoryItem(15, 10));

		// zombieActor = new Actor(idGenerator.getNextId());
		//
		// TransformComponent animatedActorTransformComponent = new
		// TransformComponent();
		// animatedActorTransformComponent.setOwner(zombieActor);
		// animatedActorTransformComponent.afterInit();
		// animatedActorTransformComponent.setPosition(new Vector3f(10, 15,
		// 10));
		// animatedActorTransformComponent.setScale(new Vector3f(0.25f, 0.25f,
		// 0.25f));
		// animatedActorTransformComponent.setRotation(new Vector3f((float)
		// Math.toRadians(-90.0), 0,
		// 0));
		//
		// PhysicsComponent zombiePhysicsComponent = new PhysicsComponent(new
		// Vector3f(0.9f, 1.7f,
		// 0.9f));
		// zombiePhysicsComponent.addOffset(new Vector3f(-0.30f, -0.3f,
		// -0.30f));
		// zombiePhysicsComponent.setShouldApplyGravity(true);
		// zombiePhysicsComponent.setOwner(zombieActor);
		// zombiePhysicsComponent.afterInit();
		//
		// // ModelRenderComponent modelRenderComponent = new
		// // ModelRenderComponent("anim");
		// // modelRenderComponent.setOwner(zombieActor);
		// // modelRenderComponent.afterInit();
		//
		// AnimatedActorComponent animatedActorComponent = new
		// AnimatedActorComponent(
		// "resources/model/zombie.md5mesh");
		// animatedActorComponent.setOwner(zombieActor);
		// animatedActorComponent.afterInit();
		//
		// HittableActorComponent hitActorComponent = new
		// HittableActorComponent();
		// hitActorComponent.setOwner(zombieActor);
		// hitActorComponent.afterInit();
		//
		// HealthComponent zombieHealthComponent = new HealthComponent(20);
		// zombieHealthComponent.setOwner(zombieActor);
		// zombieHealthComponent.afterInit();
		//
		// ZombieAI zombieAiComponent = new ZombieAI(30);
		// zombieAiComponent.setOwner(zombieActor);
		// zombieAiComponent.afterInit();
		//
		// ActorMessageControllerComponent zombieMessageControl = new
		// ActorMessageControllerComponent();
		// zombieMessageControl.setOwner(zombieActor);
		// zombieMessageControl.afterInit();
		//
		// // md5Model = new Md5Model("resources/model/zombie.md5mesh");
		// // md5Model.load();
		// // md5Model.playAnimation("idle", true, false, false, 0.0f);
		// // md5Model.setPlaybackSpeed(1);
		// // md5Model.uploadVertexBuffers();
		// // RenderableModelNode[] nodes = md5Model.getRenderableNodes();
		// // modelLoader.addLoadedModel("anim", nodes);
		//
		// zombieActor.addComponent(TransformComponent.TRANSFORM_COMPONENT_NAME,
		// animatedActorTransformComponent);
		// zombieActor.addComponent(AnimatedActorComponent.ANIMATED_ACTOR_COMPONENT,
		// animatedActorComponent);
		// zombieActor.addComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME,
		// zombiePhysicsComponent);
		// zombieActor.addComponent(HittableActorComponent.HITTABLE_ACTOR_COMPONENT_NAME,
		// hitActorComponent);
		// zombieActor.addComponent(HealthComponent.HEALTH_COMPONENT_NAME,
		// zombieHealthComponent);
		// zombieActor.addComponent(ZombieAI.ZOMBIE_AI_COMPONENT_NAME,
		// zombieAiComponent);
		// zombieActor.addComponent(ActorMessageControllerComponent.ACTOR_MESSAGE_COMPONENT_NAME,
		// zombieMessageControl);
		//
		// actorSearchService.addActor(zombieActor);
	}

	private void registerAllViews() {
		viewStack.registerView("handItemView", new HandItemView(0));
		viewStack.registerView("inventoryView", new InventoryView(1));
		viewStack.registerView("lifeShowingView", new LifeShowingView(2));
		viewStack.registerView("craftingMenu", new CraftingMenu(3));
	}

	public void run() {
		double deltaTime = 0.0;
		while (running) {
			running = running && !window.isCloseRequested();
			while (initialized && running) {

				long startTime = System.currentTimeMillis();
				long startNanoTime = System.nanoTime();

				render();
				if (zombieActor != null) {
					PhysicsComponent phy = ((PhysicsComponent) zombieActor
							.getComponent(PhysicsComponent.PHYSICS_COMPONENT_PROPERTY_NAME));
					if (phy.isTouchGround()) {
						// phy.addImpulse(new Vector3f(0, 1, 0));
					}
				}
				updateUpdatables(deltaTime);

				if (running) {
					// System.out.println("Render + update: " +
					// (System.nanoTime() -
					// startNanoTime)
					// / 1000000.0);
					GL11.glFinish();
					long endNanoTime = System.nanoTime();
					synchronizeFramerate();
					long endTime = System.nanoTime();
					updateFpsAndFrameTime(startTime, startNanoTime, endNanoTime);
					// deltaTime = (startTime - endTime) / 1000.0;
					deltaTime = 1.0 / 60.0;
				}
			}
			if (running) {
				messageHandler.distributeMessages();
				if (!initialized) {
					sleepFor(50);
				}
			}
		}
	}

	private void sleepFor(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interrupted");
		}
	}

	private void updateFpsAndFrameTime(long startTime, long startNanoTime, long endNanoTime) {
		++frames;
		long endTime = System.currentTimeMillis();
		lastFps += endTime - startTime;
		if (lastFps > 1000) {
			double deltaNanoTime = (endNanoTime - startNanoTime) / 1000000.0d;
			double fps = (double) frames / (lastFps / 1000.0);
			window.setTitleBarText(fps, deltaNanoTime);
			frames = 0;
			lastFps = 0;
		}
	}

	private void updateUpdatables(double deltaTime) {
		activeCamera.update();
		TransformComponent transform = (TransformComponent) testActor
				.getComponent(TransformComponent.TRANSFORM_COMPONENT_NAME);
		// TODO: make a camera component and put these there
		transform.setPosition(activeCamera.getPosition());
		transform.setRotation(activeCamera.getOrientation());
		Vector3f cameraPosition = transform.getPosition();
		activeCamera.setPosition(cameraPosition);

		physics.update(deltaTime);

		activeCamera.setPosition(transform.getPosition());

		input.update();
		actorSearchService.updateActors(deltaTime);
		gameMapRenderer.startAsynchBatchPrepare(activeCamera);
		blockUpdateManager.update(deltaTime);
		messageHandler.distributeMessages();
		pathSearchService.update(deltaTime);
	}

	private void synchronizeFramerate() {
		if (desiredFps != 0) {
			Display.sync(desiredFps);
		}
		Display.update();

	}

	private void clearScreen() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}

	private void render() {
		clearScreen();
		activeCamera.updateOpenglTransformation();
		gameMapRenderer.render(activeCamera.getFrustum());
		hudRenderer.render();
	}

	public void setServer(boolean isServer) {
		this.isServer = isServer;
	}

	public void setSinglePlayer(boolean isSinglePlayer) {
		this.isSinglePlayer = isSinglePlayer;
	}

	@Override
	public boolean receiveMessage(Message message) {
		if (message.getType() == MessageTypes.ESCAPE_REQUESTED) {
			running = false;
		} else if (message.getType() == MessageTypes.USER_CONNECTION_RESULT_MESSAGE) {
			UserConnectionResultMessage userMessage = (UserConnectionResultMessage) message;
			ApplicationContext context = Singleton.getInstance().getContext();
			createIdGeneratorWithRangeIfNotExists(context, userMessage.getIdLowerRange(),
					userMessage.getIdUpperRange());

			// TODO: get position from message
			createCameraWithOrientationIfNotAlreadyExists(new Vector3f(10, 15, 10), new Vector3f(0,
					0, 0));

			initializeAfterNetworkConnected();
		}
		return false;
	}

	private void createIdGeneratorWithRangeIfNotExists(ApplicationContext context, int lowerRange,
			int upperRange) {
		idGenerator = context.getBean(MyIdGenerator.class);
		if (!idGenerator.wasInitialized()) {
			idGenerator.setLowerBound(lowerRange);
			idGenerator.setUpperBound(upperRange);
			idGenerator.setInitialized();
		}
	}

	private void createCameraWithOrientationIfNotAlreadyExists(Vector3f position,
			Vector3f orientation) {
		if (activeCamera == null) {
			activeCamera = new Camera(45.0f);
			activeCamera.setPosition(position);
			activeCamera.setRotation(orientation);
		}
	}
}
