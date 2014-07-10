package net.evermemo.mport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class App {
    
    private static final Log log = LogFactory.getLog(App.class);
    
    private boolean quitFlag = true;
    
    public static void main(String[] args) throws Exception {
        System.out.println("Hello World!");
        new App().run();
    }

    private void run() throws Exception {
        Selector selector = Selector.open();
        registerAcceptor(selector);
        try {
            while (quitFlag) {
                int selectedSize = selector.select(100);
                if (selectedSize > 0) {
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iter = keys.iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isValid() && key.isAcceptable()) {
                            handleAccept(key, selector);
                        }
                        if (key.isValid() && key.isReadable()) {
                            handleRead(key);
                        }
                        if (key.isValid() && key.isWritable()) {
                            handleWrite(key);
                        }
                        iter.remove();
                    }
                }
            }
        } finally {
            selector.close();
        }
    }

    private void handleWrite(SelectionKey key) {
        
    }

    private void handleRead(SelectionKey key) {
        ByteBuffer bb = ByteBuffer.allocate(10240);
        SocketChannel sc = (SocketChannel) key.channel();
        try {
            int read = 0;
            while ((read = sc.read(bb)) > 0);
            if (read != -1) {
                bb.flip();
                Multiplexing multiplexing = Multiplexing.guess(bb);
                multiplexing.forward();
                key.attach(multiplexing);
            } else {
                cancelKey(key);
            }
        } catch (IOException e) {
            log.info(e);
            cancelKey(key);
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        try {
            SocketChannel sc = ssc.accept();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            log.error("Failed to accept new connection");
        }
    }

    private void registerAcceptor(Selector selector) throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress("0.0.0.0", 8703));
        ssc.configureBlocking(false);
        ssc.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    private void cancelKey(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException e1) {
            log.error(e1);
        }
        key.cancel();
    }
}
