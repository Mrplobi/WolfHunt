package WolfHunt;
public enum State 
{
	NIGHTTIME,WEREWOLF,DAYTIME,VOTETIME,DEATHTIME;
	private static State[] vals = values();
    public State next()
    {
        return vals[(this.ordinal()+1) % vals.length];
    }
}