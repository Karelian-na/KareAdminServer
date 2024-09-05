package cn.karelian.kas.utils;

public final class NonEmptyStrategy {
	public static final byte NONE = (byte) 0x0000;
	public static final byte QUERY = (byte) 0x0001;
	public static final byte ADD = (byte) 0x0002;
	public static final byte EDIT = (byte) 0x0004;
	public static final byte DELETE = (byte) 0x0008;

	public static final int ADDEDIT = ADD | EDIT;
	public static final int OPERATION = ADD | EDIT | DELETE;
	public static final byte ALL = (byte) 0x007F;
}