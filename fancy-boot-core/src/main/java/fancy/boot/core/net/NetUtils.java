package fancy.boot.core.net;

import fancy.boot.core.http.NetException;
import lombok.experimental.UtilityClass;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * 网络工具类.
 *
 * @author Fan
 */
@UtilityClass
public class NetUtils {

    /**
     * 获取本地主机.
     *
     * @return {@link InetAddress}
     */
    public static InetAddress getLocalHost() {
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            throw new NetException(e);
        }
    }

    /**
     * 获取本地主机硬件地址.
     *
     * @return {@code byte[]}
     */
    public static byte[] getLocalHardwareAddress() {
        return getHardwareAddress(getLocalHost());
    }

    /**
     * 获取指定 {@link InetAddress} 的硬件地址.
     *
     * @param addr {@link InetAddress}
     * @return {@code byte[]}
     */
    public static byte[] getHardwareAddress(InetAddress addr) {
        try {
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(addr);
            return null == networkInterface ? null : networkInterface.getHardwareAddress();
        } catch (SocketException e) {
            throw new NetException("Failed to get hardware address for address: " + addr.getHostAddress(), e);
        }
    }
}
