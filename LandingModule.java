package celeste.Simulator.LandingModule;
/*
 * A class that contains the LandingModule with methods required to guide the Rocket for a sucesseful landing on Titan
 */
import java.util.ArrayList;
import celeste.Simulator.Vector2;

public class LandingModule {
	private static Vector2 locationNeeded;
	
	private double maxAcc;
	private final double maxAccSides = 0.10 * Math.PI;
	private double timeStep;
	private double timePassed = 0;
	
	private static Rocket spaceship;
	private ArrayList<Vector2> locations = new ArrayList<>();
	private WindModule wind = new WindModule(true);
	
	private Vector2 tempLoc = new Vector2();
	private Vector2 tempVeloc = new Vector2();
	
	private double theta = 0;
	private double thetaVel = 0;
	
	
	public LandingModule(Vector2 desiredLoc, Rocket spaceship, double timeStep) {
		LandingModule.locationNeeded = desiredLoc;
		LandingModule.spaceship = spaceship;
		this.timeStep = timeStep;
		maxAcc = 30000 / spaceship.getMass();
	}
	
	public ArrayList<Vector2> fullLaunch(){
		estimateDisplacementAfterLaunching();
		correctX();
		enterSpaceShip();
		return locations;
	}
	
	public void estimateDisplacementAfterLaunching() {

		double t = timeStep;
		double sSpeedUp = 0;
		double sNeeded = 0;
		double velFinal = 0;
		while (sNeeded < locationNeeded.y) {
			sSpeedUp = Rocket.GRAV_TITAN * t * t * 0.5;
			double vNow = Rocket.GRAV_TITAN * t;
			double sSlowDown = vNow * t - 0.5 * Rocket.GRAV_TITAN * t * t;
			velFinal = vNow - Rocket.GRAV_TITAN * t;
			sNeeded = sSpeedUp + sSlowDown;
			
			t = t + timeStep;
		}
		System.out.println(sNeeded);
		System.out.println(velFinal);
		
		while (tempLoc.y < sSpeedUp){
			tempLoc.y = 0.5 * timePassed * timePassed * (Rocket.GRAV_TITAN);
			tempVeloc.y = timePassed * (Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
		}
		
		System.out.println("needed to reach : " + sSpeedUp + " reached : " + tempLoc.y);
		System.out.println("current yvel : " + tempVeloc.y);
		System.out.println("current time : " + timePassed);
		System.out.println("current x : " + tempLoc.x);
		System.out.println();
	}
	
	public void correctX() {
		
		double thetaNeeded = 0.5 * Math.PI;
		double timeNeeded = timeStep;
		double temporaryV = 1000;
		double tempMax;
		double tempNeededVel = 0;
				
		if (tempLoc.x > 0) {
			thetaNeeded = -thetaNeeded;
			temporaryV = -temporaryV;
			tempMax = -maxAccSides;
					
			while (temporaryV < tempMax) {
				tempNeededVel = thetaNeeded / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		else {
			while (temporaryV > maxAccSides) {
				tempNeededVel = thetaNeeded / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		
		theta = timeNeeded * 0.5 * timeNeeded * temporaryV;
		thetaVel = timeNeeded * temporaryV;
		
		System.out.println("Theta needed : " + thetaNeeded);
		System.out.println("Theta after first correction : " + theta);
		System.out.println("Theta velocity after first correction : " + thetaVel);
		System.out.println();
		
		double secondTimeNeeded= (thetaNeeded - theta) / (0.5 * thetaVel);
		temporaryV = -(thetaVel / secondTimeNeeded);
		
		theta = theta + secondTimeNeeded * 0.5 * secondTimeNeeded * temporaryV + thetaVel * secondTimeNeeded;
		thetaVel = thetaVel + secondTimeNeeded * temporaryV;
		
		
		double timeTemp = timePassed + timeNeeded + secondTimeNeeded;
		double startVelY = tempVeloc.y;
		double startY = tempLoc.y;
		double time = timeStep;

		while (timePassed< (timeTemp)){
			tempLoc.x = tempLoc.x + wind.calcDisplacement(spaceship, tempLoc.y);
			tempLoc.y = startY + startVelY * time +  0.5 * time * time * (-Rocket.GRAV_TITAN);
			tempVeloc.y = startVelY + time * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
			time = time + timeStep;
		}
		
		System.out.println("Theta after second correction : " + theta);
		System.out.println("Theta velocity after second correction : " + thetaVel);
		System.out.println("x after this correction : " + tempLoc.x);
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction : " + tempVeloc.y);
		System.out.println();
		
		double uTemp = 1000;
		double sNeeded = (locationNeeded.x)/ 5;
		time = timeStep;
		double maxVel, aNeeded;
		
		
		System.out.println("Needed s : " + sNeeded);
		
		while (uTemp > maxAcc) {
			maxVel = 2 * (sNeeded / time); 
			aNeeded =(maxVel)/ (time);
			uTemp = (aNeeded / (Math.sin(theta)));
			time = time + timeStep;
		} 
		
		timeTemp = timePassed + time;
		double extraTime = 0;
		double storeY = tempLoc.y;
		double storeYVel = tempVeloc.y;

		while (timePassed < timeTemp) {
			tempLoc.x = 0.5 * uTemp *  Math.sin(theta) * extraTime * extraTime;
			tempVeloc.x = tempVeloc.x + timeStep  * uTemp * Math.sin(theta);
			tempLoc.y = storeY + storeYVel * extraTime +  0.5 * extraTime * extraTime * (-Rocket.GRAV_TITAN);
			tempVeloc.y = storeYVel + extraTime * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
			extraTime = extraTime + timeStep;
		}
		
		System.out.println("x after this correction : " + tempLoc.x);
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction : " + tempVeloc.y);
		System.out.println("x velocity : " + tempVeloc.x);
		System.out.println();
		
		thetaNeeded = - 0.5 * Math.PI;
		timeNeeded = timeStep;
		temporaryV = 1000;
		tempMax = 0;
		tempNeededVel = 0;
				
		if (tempLoc.x > 0) {
			temporaryV = -temporaryV;
			tempMax = -maxAccSides;
					
			while (temporaryV < tempMax) {
				tempNeededVel = thetaNeeded / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		else {
			while (temporaryV > maxAccSides) {
				tempNeededVel = thetaNeeded / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		
		theta = theta + (timeNeeded * 0.5 * timeNeeded * temporaryV);
		thetaVel = thetaVel + timeNeeded * temporaryV;
		
		secondTimeNeeded= Math.abs((thetaNeeded - theta) / (0.5 * thetaVel));
		temporaryV = -(thetaVel / secondTimeNeeded);
		
		theta = theta + secondTimeNeeded * 0.5 * secondTimeNeeded * temporaryV + thetaVel * secondTimeNeeded;
		thetaVel = thetaVel + secondTimeNeeded * temporaryV;
		System.out.println("Time needed for theta correction :  " + (timeNeeded + secondTimeNeeded));
		
		timeTemp = timePassed + timeNeeded + secondTimeNeeded;
		
		storeY = tempLoc.y;
		storeYVel = tempVeloc.y;
		time = timeStep;
		while (timePassed < (timeTemp)) {
			tempLoc.x = tempLoc.x + tempVeloc.x * timeStep;
			tempLoc.y = storeY + storeYVel * time +  0.5 * time * time * (-Rocket.GRAV_TITAN);
			tempVeloc.y = storeYVel + time * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed  + timeStep;
			time = time + timeStep;
		}
		
		System.out.println("x after this correction : " + tempLoc.x);
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction : " + tempVeloc.y);
		System.out.println("Tilt after this correction : " + theta);
		System.out.println("Tilt velocity after this correction : " + thetaVel);
		System.out.println();
		
		System.out.println("Needed s : " + (locationNeeded.x - tempLoc.x));
		timeNeeded = Math.abs((locationNeeded.x - tempLoc.x) / (0.5 * tempVeloc.x));
		System.out.println("Time needed to slow down : " + timeNeeded);
		
		double accNeeded = (tempVeloc.x / timeNeeded);
		uTemp = Math.abs((accNeeded / Math.sin(theta)));
		System.out.println("u needed : " + uTemp);
		
		timeTemp = timePassed + timeNeeded;
		double startVeloc = tempVeloc.x;
		double startVelocY = tempVeloc.y;
		double timePassedNow = timeStep;
		double startX = tempLoc.x;
		while (timePassed < (timeTemp)) {
			tempLoc.x = startX + startVeloc * timePassedNow + ( 0.5 * uTemp * Math.sin(theta) * timePassedNow * timePassedNow);
			tempVeloc.x = startVeloc+ timePassedNow * Math.sin(theta) * uTemp;
			tempLoc.y = tempLoc.y + startVelocY * timeStep +  0.5 * timeStep * timeStep * (-Rocket.GRAV_TITAN);
			tempVeloc.y = tempVeloc.y + timeStep * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
			timePassedNow = timePassedNow + timeStep;
		}
		
		System.out.println("x after this correction : " + tempLoc.x);
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction :" + tempVeloc.y);
		System.out.println("x velocity : " + tempVeloc.x);
		System.out.println();
	}
	
	public void enterSpaceShip() {
	
		double sTheta = -theta;
		double timeNeeded = timeStep;
		double temporaryV = 1000;
		double tempMax = 0;
		double tempNeededVel = 0;
		
		System.out.println(sTheta);

		if (sTheta < 0) {
			temporaryV = -temporaryV;
			tempMax = -maxAccSides;
					
			while (temporaryV < tempMax) {
				tempNeededVel = sTheta / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		else {
			while (temporaryV > maxAccSides) {
				tempNeededVel = sTheta / timeNeeded;
				temporaryV = tempNeededVel / timeNeeded;
				timeNeeded = timeNeeded + timeStep;
			}
		}
		
		theta = theta + timeNeeded * 0.5 * timeNeeded * temporaryV;
		thetaVel = timeNeeded * temporaryV; 
		System.out.println("Theta now : " + theta + " Theta velocity : " + thetaVel);
		
		double secondTimeNeeded = timeStep;
		
		secondTimeNeeded= (0.5 * sTheta) / (0.5 * thetaVel);
		temporaryV = -(thetaVel / secondTimeNeeded);
		
		theta = theta + secondTimeNeeded * 0.5 * secondTimeNeeded * temporaryV + thetaVel * secondTimeNeeded;
		thetaVel = thetaVel + secondTimeNeeded * temporaryV;
		
		double timeTemp = timePassed;
		double storeYVel = tempVeloc.y;
		double timeNow = timeStep;
		double storeY = tempLoc.y;
		while (timePassed< (timeTemp + timeNeeded + secondTimeNeeded)){
			tempLoc.y = storeY + storeYVel * timeNow +  0.5 * timeNow * timeNow * (-Rocket.GRAV_TITAN);
			tempVeloc.y = storeYVel + timeNow * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
			timeNow = timeNow + timeStep;
		}
		
		System.out.println("x after this correction : " + tempLoc.x);
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction : " + tempVeloc.y);
		System.out.println("x velocity after this correction : " + tempVeloc.x);
		System.out.println("Tilt after this correction : " + theta);
		System.out.println("Tilt vel after this correction : " + thetaVel);
		System.out.println();
		
		
		timeTemp = timePassed;
		storeYVel = tempVeloc.y;
		timeNow = timeStep;
		storeY = tempLoc.y;
		while (tempLoc.y < (locationNeeded.y)){
			tempLoc.y = storeY + storeYVel * timeNow +  0.5 * timeNow * timeNow * (-Rocket.GRAV_TITAN);
			tempVeloc.y = storeYVel + timeNow * (-Rocket.GRAV_TITAN);
			
			locations.add(tempLoc.duplicate());
			timePassed = timePassed +  timeStep;
			timeNow = timeNow + timeStep;
		 }
		
		System.out.println("y after this correction : " + tempLoc.y);
		System.out.println("y velocity after this correction : " + tempVeloc.y);
	}
}
