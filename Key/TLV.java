package Key;

import java.util.*;
//import com.tplink.smb.easySmartUtility.global.*;
import java.nio.charset.*;

public class TLV
{
    private short type;
    private ArrayList<Byte> mZ;
    
    public TLV() {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = 0;
    }
    
    public TLV(final short type) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
    }
    
    public TLV(final short type, final byte[] array) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        for (int i = 0; i < array.length; ++i) {
            this.mZ.add(array[i]);
        }
    }
    
    public TLV(final short type, final Byte[] array) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        for (int i = 0; i < array.length; ++i) {
            this.mZ.add(array[i]);
        }
    }
    
    public TLV(final short type, final byte b) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        this.C(b);
    }
    
    public TLV(final short type, final short n) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        this.S(n);
    }
    
    public TLV(final short type, final int n) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        this.e(n);
    }
    
    public TLV(final short type, final String s) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        this.p(s);
    }
    
    public TLV(final short type, final TLV tlv) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = type;
        this.V(tlv);
    }
    
    public TLV(final byte[] array) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = b(new byte[] { array[0], array[1] });
        final short b = b(new byte[] { array[2], array[3] });
        //try {
            for (int i = 4; i < 4 + b; ++i) {
                this.mZ.add(array[i]);
            }
        //}
        /*catch (final Exception ex) {
            darkness.Code("Exception in new TLV(byte[] tlv)", new Object[0]);
        }*/
    }
    
    public TLV(final byte[] array, final int n) {
        super();
        this.mZ = new ArrayList<Byte>();
        this.type = b(new byte[] { array[n], array[n + 1] });
        final short b = b(new byte[] { array[n + 2], array[n + 3] });
        //try {
            for (int i = n + 4; i < n + 4 + b; ++i) {
                this.mZ.add(array[i]);
            }
        //}
        /*catch (final Exception ex) {
            darkness.Code("Exception in new TLV(byte[] tlv, int offset)", new Object[0]);
        }*/
    }
    
    public TLV C(final short type) {
        this.type = type;
        return this;
    }
    
    public TLV L(final byte[] array) {
        this.type = b(array);
        return this;
    }
    
    public short getType() {
        return this.type;
    }
    
    public short getLength() {
        return (short)this.mZ.size();
    }
    
    public byte[] getValue() {
        final byte[] array = new byte[this.mZ.size()];
        for (int i = 0; i < this.mZ.size(); ++i) {
            array[i] = this.mZ.get(i);
        }
        return array;
    }
    
    public TLV C(final byte b) {
        this.mZ.add(b);
        return this;
    }
    
    public TLV S(final short n) {
        this.mZ.add((byte)(n >>> 8));
        this.mZ.add((byte)(n & 0xFF));
        return this;
    }
    
    public TLV e(final int n) {
        this.mZ.add((byte)(n >>> 24));
        this.mZ.add((byte)(n >> 16 & 0xFF));
        this.mZ.add((byte)(n >> 8 & 0xFF));
        this.mZ.add((byte)(n & 0xFF));
        return this;
    }
    
    public TLV p(final String s) {
        final byte[] bytes = s.getBytes();
        for (int i = 0; i < bytes.length; ++i) {
            this.mZ.add(bytes[i]);
        }
        this.mZ.add((byte)0);
        return this;
    }
    
    public TLV a(final byte[] array) {
        for (int i = 0; i < array.length; ++i) {
            this.mZ.add(array[i]);
        }
        return this;
    }
    
    public TLV V(final TLV tlv) {
        return this.a(tlv.toBytes());
    }
    
    public byte getByte() {
        return this.mZ.remove(0);
    }
    
    public byte[] getByteArray() {
        return this.getValue();
    }
    
    public short getShort() {
        return (short)((this.mZ.remove(0) << 8 & 0xFF00) | (this.mZ.remove(0) & 0xFF));
    }
    
    public int getInt() {
        return this.mZ.remove(0) << 24 | this.mZ.remove(0) << 24 >>> 8 | (this.mZ.remove(0) << 8 & 0xFF00) | (this.mZ.remove(0) & 0xFF);
    }
    
    public String getString() {
        final Charset forName = Charset.forName("UTF-8");
        final ArrayList list = new ArrayList();
        while (this.mZ.get(0) != 0) {
            list.add(this.mZ.remove(0));
        }
        this.mZ.remove(0);
        final byte[] bytes = new byte[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            bytes[i] = (byte)list.get(i);
        }
        return new String(bytes, forName);
    }
    
    public TLV aW() {
        final short b = b(new byte[] { this.mZ.get(2), this.mZ.get(3) });
        final byte[] array = new byte[4 + b];
        //try {
            for (int i = 0; i < 4 + b; ++i) {
                array[i] = this.mZ.remove(0);
            }
        //}
        /*catch (final Exception ex) {
            darkness.Code("Exception in getTLV()", new Object[0]);
        }*/
        return new TLV(array);
    }
    
    public byte[] toBytes() {
        final byte[] array = new byte[4 + this.mZ.size()];
        final byte[] f = F(this.type);
        final byte[] f2 = F((short)this.mZ.size());
        array[0] = f[0];
        array[1] = f[1];
        array[2] = f2[0];
        array[3] = f2[1];
        for (int i = 0; i < this.mZ.size(); ++i) {
            array[4 + i] = this.mZ.get(i);
        }
        return array;
    }
    
    public static short b(final byte[] array) {
        if (array.length != 2) {
            return 0;
        }
        return (short)((array[0] << 8 & 0xFF00) | (array[1] & 0xFF));
    }
    
    public static byte[] F(final short n) {
        return new byte[] { (byte)(n >>> 8), (byte)(n & 0xFF) };
    }
    
    public static int c(final byte[] array) {
        if (array.length != 4) {
            return 0;
        }
        return array[0] << 24 | array[1] << 24 >>> 8 | (array[2] << 8 & 0xFF00) | (array[3] & 0xFF);
    }
    
    public static long d(final byte[] array) {
        return (long)c(array) & 0xFFFFFFFFL;
    }
    
    public static byte[] f(final int n) {
        return new byte[] { (byte)(n >>> 24), (byte)(n >> 16 & 0xFF), (byte)(n >> 8 & 0xFF), (byte)(n & 0xFF) };
    }
    
    public static String e(final byte[] array) {
        final StringBuilder sb = new StringBuilder("");
        if (array == null || array.length <= 0) {
            return null;
        }
        for (int i = 0; i < array.length; ++i) {
            final String hexString = Integer.toHexString(array[i] & 0xFF);
            if (hexString.length() < 2) {
                sb.append(0);
            }
            sb.append(hexString);
        }
        return sb.toString();
    }
    
    public static String f(final byte[] array) {
        final StringBuilder sb = new StringBuilder("");
        if (array == null || array.length <= 0) {
            return null;
        }
        for (int i = 0; i < array.length; ++i) {
            final String string = Integer.toString(array[i] & 0xFF);
            if (string.length() < 2) {
                sb.append(0);
            }
            sb.append(string);
        }
        return sb.toString();
    }
    
    public static String g(final byte[] array) {
        final StringBuilder sb = new StringBuilder("");
        if (array == null || array.length <= 0) {
            return null;
        }
        for (int i = 0; i < array.length; ++i) {
            sb.append(String.format("%s", (char)(array[i] & 0xFF)));
        }
        return sb.toString();
    }
    
    public static String bytesToString(final byte[] array) {
        final StringBuilder sb = new StringBuilder("");
        if (array == null || array.length <= 0) {
            return null;
        }
        for (int i = 0; i < array.length - 1; ++i) {
            sb.append(String.format("%s", (char)(array[i] & 0xFF)));
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        if (this.type == 514) {
            return new String(String.format("[type:%x, length:%d, %s]", this.type, this.getLength(), e(new of().q(bytesToString(this.getValue())))));
        }
        return new String(String.format("[type:%x, length:%d, %s]", this.type, this.getLength(), e(this.getValue())));
    }
    
    public static void main(final String[] array) {
        final TLV tlv = new TLV();
        tlv.p("this");
        tlv.e(12345678);
        tlv.S((short)1234);
        tlv.C((byte)12);
        final TLV v = new TLV().V(tlv);
        System.out.println(tlv.getString());
        System.out.println(tlv.getInt());
        System.out.println(tlv.getLength());
        System.out.println(tlv.getShort());
        System.out.println(tlv.getByte());
        System.out.println(tlv.getLength());
        System.out.println("-----------");
        final TLV aw = v.aW();
        System.out.println(aw.getString());
        System.out.println(aw.getInt());
        System.out.println(aw.getLength());
        System.out.println(aw.getShort());
        System.out.println(aw.getByte());
        System.out.println(aw.getLength());
    }
}

