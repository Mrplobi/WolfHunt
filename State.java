package WolfHunt;
public enum State 
{
	NIGHTTIME,WEREWOLF,DAYTIME,VOTETIME;
	private static State[] vals = values();
    public State next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }
}