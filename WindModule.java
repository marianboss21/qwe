package celeste.Simulator.LandingModule;

import java.util.ArrayList;

public class WindModule {

    private double wind;
    public final double airDensityTitan = 1.23995;
    public final double airDensityEarth = 1.2041;
    public final double area = 17*4.5;
    private double relativeWindSpeed;
    public double force;
    private double accByWind;
    private static double TIME_SLICE=1;
    private boolean titan;
  
    public WindModule(boolean titan){
        wind = 0;
        this.titan = titan;
        
    }

    public double calcWindSpeed(double kmFromSurface) {
    
        double windSpeed = (119.7 / 120) * kmFromSurface + 0.3;

        if (windSpeed>100) {
            windSpeed = 100;
        }

        double range = (1.2 - 0.8);
        double randWindSpeed = windSpeed * ((Math.random() * range) + 0.8);

        double direction = Math.random();

        if (wind == 0 && direction >=0.5) {
            randWindSpeed = -randWindSpeed;
            wind = randWindSpeed;
        }

        else if (wind == 0 && direction <0.5) {
            wind = randWindSpeed;
        }

        else if (wind < 0 && direction>=0.8) {
            wind = randWindSpeed;
        }
        else if (wind < 0 && direction<0.8){
            
        	wind = -randWindSpeed;
        }

        else if (wind > 0 && direction>=0.8) {
         
            wind = -randWindSpeed;
        }
        else if (wind > 0 && direction<0.8){
            wind = randWindSpeed;
        }
        return wind;
    }

    public double accByWind(Rocket s, double kmtosurface){
        calcWindSpeed(kmtosurface);
        calculateForce(s);
        accByWind = s.force/s.getMass();
        return accByWind;
    }

    public void calculateForce(Rocket s){
        calculateRelativeWindSpeed(s);
        if (titan) {
        	s.force = (area/2)*airDensityTitan*(relativeWindSpeed*relativeWindSpeed);
    	}
        else {
        	s.force = (area/2) * airDensityEarth*(relativeWindSpeed*relativeWindSpeed);
        }
        
        if (s.getRelativeWindSpeed()<0) {
            s.force = -s.force;
        }
    }
    
    public ArrayList<Double> calculateForcesForWholeTraject(Rocket spaceship, double kilometresStart) {

    	ArrayList<Double> forces = new ArrayList(); 
  
    	while(kilometresStart> -1) { 
    		calcWindSpeed(kilometresStart);
    		calculateForce(spaceship);
    		forces.add(this.getForce());
    		kilometresStart = kilometresStart - 1;
    	}
    	
    	return forces;
    }
    
    
    public void calculateRelativeWindSpeed(Rocket s){
        relativeWindSpeed = s.getWind()-s.getVelocity().getX();
    }

    public double calcDisplacement(Rocket s, double kmtosurface){
        double displacement = s.getVelocity().getX()*TIME_SLICE + 0.5*(accByWind(s, kmtosurface)) * TIME_SLICE*TIME_SLICE;
        return displacement;
    }
    
    public double getWind() {
        return this.wind;
    }
    public double getAccByWind() {
        return this.accByWind;
    }

    public double getAirDensityTitan() {
        return airDensityTitan;
    }
    
    public double getAirDensityEarth() {
    	return airDensityEarth;
    }

    public double getArea() {
        return area;
    }

    public double getRelativeWindSpeed() {
        return relativeWindSpeed;
    }

    public double getForce() {
        return force;
    }
    
    public double calcTilt(Rocket s, double kmToSurface){
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

        if (s.getWind()<0)
            s.addTilt(tiltInRadians);
        else
            s.addTilt(-tiltInRadians);

        return tiltInRadians;
    }
}
