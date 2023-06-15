package celeste.Simulator.LandingModule;

import celeste.Simulator.Vector2;

public class Rocket extends WindModule {

	private static final int SEC_IN_MINUTE = 60;
	private static final int SEC_IN_HOUR = SEC_IN_MINUTE * 60;
	private static final int SEC_IN_DAY = SEC_IN_HOUR * 24;
	private static final int SEC_IN_YEAR = 31556926;
	private double elapsedSeconds = 0;

	private double mass; 
	private Vector2 velocity;
	private Vector2 acceleration;
	private Vector2 location;
	private double height;
	private double width;
	private boolean titan;
	private WindModule windModule;
	
	private double tilt = 0; 
	private double angularVelocity = 0;
	private double angularAcceleration = 0;
	private double torque = 0;
	private double accelerationByMainThrusters = 0;
	
	public static final double GRAV_TITAN = 1.352; 
	protected static final double GRAV_EARTH = 9.81;
	public static final double DENSITY_TITAN = 1.23995416;
	public static final double DENSITY_EARTH = 1.2041;
	public static final double DRAG_CO = 0.10;
	
	private static final double FINAL_ANGLE = 0.02;
	private static final double FINAL_ANGULAR_VELOCITY = 0.01;
	private static final double LANDING_X_TOLERANCE = 0.01; 
	private static final double FINAL_X_VELOCITY = 0.1;
	private static final double FINAL_Y_VELOCITY = 0.1;

		public Rocket(double mass, Vector2 velocity, Vector2 acceleration, Vector2 location, double height, double width, boolean titan) {
			super(titan); 
			this.titan = titan;
			this.mass = mass;
			this.velocity = velocity;
			this.acceleration = acceleration;
			this.location = location;
			this.height = height;
			this.width = width;
			windModule = new WindModule(titan);
		}

	    public void setMass(double mass) {
	        this.mass = mass;
	    }

	    public double getMass() {
	        return mass;
	    }

	    public void  setVelocity(Vector2 vel) {
	        velocity = vel;
	    }

	    public Vector2 getVelocity() {
	        return velocity;
	    }
	    
	    public void setAcceleration(Vector2 acceleration) {
	        this.acceleration = acceleration;
	    }

	    public Vector2 getAcceleration() {
	        return acceleration;
	    }

	    public void setLocation(Vector2 location) {
	    	this.location = location;
	    }
	    
	    public Vector2 getLocation() {
	    	return location;
	    }

	    public void setXLocation(double x) {
	        location.setX(x);
	    }

	    public double getXLocation() {
	    	return location.x;
	    }

	    public void setYLocation(double y) {
	    	location.setY(y);
	    }

	    public double getYLocation() {
	        return location.y;
	    }

	    public void setXVelocity(double xVelocity){
	    	this.velocity.setX(xVelocity); 
	    }

	    public double getXVelocity(){ 
	    	return this.velocity.getX(); 
	    }

	    public void setYVelocity(double yVelocity){
	        this.velocity.setY(yVelocity);
	    }

	    public double getYVelocity(){
	        return this.velocity.getY();
	    }

	    public void addTilt(double tilt) {
	        this.tilt = this.tilt + tilt;
	    }

	    public void setTilt(double tilt) {
	        this.tilt = tilt;
	    }

	    public double getTilt() {
	        return tilt;
	    }

	    public void setHeight(double length) {
	        this.height = length;
	    }

	    public double getHeight() {
	        return height;
	    }

	    public void setWidth(double width) {
	        this.width = width;
	    }

	    public double getWidth() {
	        return width;
	    }

	    public void setTorque(double torque) {
	    	this.torque = torque;
	    }
	    
	    public double getTorque(){
	        return torque;
	    }
	    
	    public void setAngularVelocity(double angVel) {
	    	this.angularVelocity = angVel;
	    }
	    
	    public double getAngularVelocity() {
	    	return angularVelocity;
	    }
	    
	    public void setAngularAcceleration(double angAcc) {
	    	this.angularAcceleration = angAcc;
	    }
	    
	    public double getAngularAcceleration() {
	    	return angularAcceleration;
	    }
	    
	    public void setAccelerationByMainThrusters(double accMa) {
	    	this.accelerationByMainThrusters = accMa;
	    }
	    
	    public double getAccelerationByMainThrusters() {
	    	return accelerationByMainThrusters;
	    }
	    
	    public void setSeconds(double d) {
			elapsedSeconds = d;
		}
		
		public double getSeconds() {
			return elapsedSeconds;
		}

	    public double calculateRotationAcceleration(){
	        this.angularAcceleration = torque;
	        return angularAcceleration;
	    }
	    
	    public void calculateCurrentTilt(double timeStep) {
	    	this.tilt = this.tilt + this.angularVelocity * timeStep;
	    }
	    
	    public void addAccelerationBySideThrusters(double timeStep) {
	    	this.angularVelocity = this.angularVelocity + this.calculateRotationAcceleration() * timeStep;
	    }
	    
	    public void addAccelerationByMainThrusters() {
	    	this.acceleration.x = this.calculateXAcceleration();
	    	this.acceleration.y = this.calculateYAcceleration();
	    }
	    
	    
	    public void update(double timeSlice) {
		
		this.windModule.calcDisplacement(this, this.getLocation().getY());
		this.getVelocity().setX(this.getVelocity().getX() + windModule.getWind()/1000);
	    this.resetAcceleration();
	   
	    this.addAccelerationByGravityForce();
	    this.addAirResistance();
	    
	    this.updateVelocityAndLocation(timeSlice);
	    elapsedSeconds = (long) (elapsedSeconds + timeSlice);
	    }
	    
	    public void updateVelocityAndLocation(double timeSlice) {
	        Vector2 oldVelocity = new Vector2(this.getVelocity());
	        updateVelocity(timeSlice);

	        Vector2 changedVelocityAverage = new Vector2(this.getVelocity()).subtract(oldVelocity).divide(2.0);
	        Vector2 averageVelocity = new Vector2(oldVelocity).add(changedVelocityAverage);
	        updateLocation(timeSlice, averageVelocity);
	    }
	    protected void updateVelocity(double timeSlice) {
	        Vector2 velocityByAcc = new Vector2(getAcceleration()).multiply(timeSlice);
	        setVelocity(getVelocity().add(velocityByAcc));
	    }

	    protected void updateLocation(double timeSlice, Vector2 averageVelocity) {
	        Vector2 locationByVelocity = new Vector2(averageVelocity).multiply(timeSlice);
	        location.add(locationByVelocity);
	    }
	    
	    public void resetAcceleration() {
	    	this.acceleration = new Vector2();
	    }

	    public double calculateXAcceleration(){
	    	double accX = accelerationByMainThrusters * Math.sin(tilt);
	        return accX;
	    }

	    public double calculateYAcceleration(){
	    	double accY;
	    	
	    	if(titan) {
	    		accY = accelerationByMainThrusters*Math.cos(tilt) - GRAV_TITAN;
	    	}
	    	else {
	    		accY = accelerationByMainThrusters*Math.cos(tilt) - GRAV_EARTH;
	    	}
	        return accY;
	    }
	    
	    public void recalculateVelocity(double timeSlice) {
	    	this.velocity.x = this.getVelocity().x + this.calculateXAcceleration() * timeSlice;
	    	this.velocity.y = this.getVelocity().y + this.calculateYAcceleration() *timeSlice;
	    }
	    
	    public void recalculateLocation(double timeSlice) {
	    	this.calculateCurrentTilt(timeSlice);
	    	recalculateVelocity(timeSlice);
	    	
	    	this.location.x = this.location.x + velocity.x * timeSlice;
	    	this.location.y = this.location.y + velocity.y * timeSlice;	
	    }

	    public boolean successfuLanding() {
	    	if (location.y <= 0) {
	    		System.out.println("y = 0");
	    		if(FINAL_ANGLE <= (this.getTilt()%(2*Math.PI)) && FINAL_ANGULAR_VELOCITY <= this.getAngularVelocity()) {
	    			System.out.println("Tilt is just about fine");
	    			if (Math.abs(this.location.x) <= LANDING_X_TOLERANCE) {
	    				System.out.print("Nice x location");
	    				if (Math.abs(this.velocity.x) <= FINAL_X_VELOCITY && Math.abs(this.velocity.y) <= FINAL_Y_VELOCITY) {
	    					System.out.println("Too small velocity");
	    					System.out.println("Welcome to Titan");
	    					return true;
	    				}
	    			}
	    		}
	    	}
	    	System.out.println("Ooops, someone failed (Marian)");
	    	return false;
	    }
	    
	    public String toString() {
	        return String.format("xAxis = %f, yAxis = %f, theta = %f", location.getX(), location.getY(), this.tilt);
	    }

	    public String getElapsedTimeAsString() {
	    	long years = (long)elapsedSeconds / SEC_IN_YEAR;
	    	long days = ((long)elapsedSeconds % SEC_IN_YEAR) / SEC_IN_DAY;
	    	long hours = ( ((long)elapsedSeconds % SEC_IN_YEAR) % SEC_IN_DAY) / SEC_IN_HOUR;
	    	long minutes = ( (((long)elapsedSeconds % SEC_IN_YEAR) % SEC_IN_DAY) % SEC_IN_HOUR) / SEC_IN_MINUTE;
	    	long seconds = ( (((long)elapsedSeconds % SEC_IN_YEAR) % SEC_IN_DAY) % SEC_IN_HOUR) % SEC_IN_MINUTE;
	    	return String.format("Years:%08d, Days:%03d, Hours:%02d, Minutes:%02d, Seconds:%02d", years, days, hours, minutes, seconds);
	    }

	    public void addPassedTime(double timeSlice) {
	    	elapsedSeconds =+ timeSlice;
	    }
	    
	    public Rocket duplicate() {
	    	Rocket copy = new Rocket(mass, velocity.duplicate(), acceleration.duplicate(), location.duplicate(), height, width, titan);
	    	copy.setAccelerationByMainThrusters(accelerationByMainThrusters);
	    	copy.setTilt(tilt);
	    	copy.setTorque(torque);
	    	copy.setSeconds(elapsedSeconds);
	    	return copy;
	    }

	    public double calculateXAcceleration(double rotationInRads, double accelerationThruster){
	        this.acceleration.setX(accelerationThruster*Math.sin(rotationInRads));
	        return this.acceleration.getX();
	    }

	    public double calculateYAcceleration(double rotationInRads, double accelerationThruster){
	        this.acceleration.setY(accelerationThruster*Math.cos(rotationInRads)- GRAV_TITAN);
	        return this.acceleration.getY();
	    }
	    
	    public void addAccelerationByGravityForce() {
	    	Vector2 grav;
	    	if (titan) {
	    		grav = new Vector2(0, -GRAV_TITAN * mass);
	    	}
	    	else {
	    		grav = new Vector2(0, -GRAV_EARTH * mass);
	    	}
	        addAccelerationByForce(grav);
	    }

	    public void addAirResistance() {
	        double resisX = 0.5 * DRAG_CO * DENSITY_TITAN * (this.height * this.width) * this.velocity.getX();
	        double resisY = 0.5 * DRAG_CO * DENSITY_EARTH * (this.width * this.width) * this.velocity.getY();
	        Vector2 resistance = new Vector2(-resisX, -resisY);
	        addAccelerationByForce(resistance);

	    }

	    public void addAccelerationByForce(Vector2 force) {
	        Vector2 accByForce = new Vector2(force);
	        accByForce.divide(mass);
	        acceleration.add(accByForce);
	    }

	    public double calcDisplacement(double kmtosurface, double timeSlice){
	        double displacement = this.getXVelocity()*timeSlice + 0.5*(accByWind(this, kmtosurface)) *timeSlice * timeSlice;
	        return displacement;
	    }

	    public double calcTilt(double kmToSurface){
	        double randTilt = (Math.random()*10);

	        if (kmToSurface <= 48)
	            randTilt = 3* randTilt;
	        else if (kmToSurface <= 72)
	            randTilt = 4.5* randTilt;
	        else if (kmToSurface <= 96)
	            randTilt = 6* randTilt;
	        else
	            randTilt = 9* randTilt;

	        double tiltInRadians = Math.toRadians(randTilt);

	        if (this.getWind()<0)
	            this.addTilt(tiltInRadians);
	        else
	            this.addTilt(-tiltInRadians);

	        return tiltInRadians;
	    }

}

