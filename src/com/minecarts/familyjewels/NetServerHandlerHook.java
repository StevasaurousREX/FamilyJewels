package com.minecarts.familyjewels;


import net.minecraft.server.*;

import java.lang.reflect.Method;
import java.util.Arrays;

import java.text.MessageFormat;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class NetServerHandlerHook extends net.minecraft.server.NetServerHandler {
    private EntityPlayer player;
    public final int[] hiddenBlocks = {14,15,16,21,48,52,54,56,73,74};
    public NetServerHandlerHook(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer player){
        super(minecraftserver,networkmanager,player);
        this.player = player;
    }



    

    @Override
    public void sendPacket(Packet packet){
        //System.out.println("Sent packet:" + packet.getClass());
         if(packet instanceof Packet14BlockDig){
             Packet14BlockDig dataPacket = (Packet14BlockDig) packet;
             if(dataPacket.e == 2){ //If it's a block break
                 int x = dataPacket.a;
                 int y = dataPacket.b;
                 int z = dataPacket.c;
                 //Mark the nearby blocks as dirty to update antixray blocks
                 player.world.notify(x + 1, y, z);
                 player.world.notify(x - 1, y, z);
                 player.world.notify(x, y + 1, z);
                 player.world.notify(x, y - 1, z);
                 player.world.notify(x, y, z - 1);
                 player.world.notify(x, y, z + 1);
             }
         } else if(packet instanceof Packet52MultiBlockChange){
             Packet52MultiBlockChange dataPacket = (Packet52MultiBlockChange) packet;
             //System.out.println("Multiblock change!");
         } else if(packet instanceof Packet51MapChunk){
            Packet51MapChunk dataPacket = (Packet51MapChunk) packet;
            int xPosition = dataPacket.a;
            int yPosition = dataPacket.b;
            int zPosition = dataPacket.c;

            int xSize = dataPacket.d;
            int ySize = dataPacket.e;
            int zSize = dataPacket.f;

            //Decompress the data
            Inflater inflater = new Inflater();
            inflater.setInput(dataPacket.g);
              try {
                inflater.inflate(dataPacket.g);
               } catch (DataFormatException dataformatexception) {
                  System.out.println("Bad compressed data format");
                    return;
                } finally {
                    inflater.end();
                }

            //System.out.println(MessageFormat.format("Packet: {0},{1},{2} (Size: {3},{4},{5})",xPosition,yPosition,zPosition,xSize,ySize,zSize));

             /*
             //Loop over each component
             for(int x=xPosition; x<xPosition + xSize; x++){
                 for(int z=xPosition; z<zPosition + zSize; z++){
                     for(int y=yPosition; y<yPosition + ySize; y++){
                         int type = player.world.getTypeId(x,y,z);
                         if(Arrays.binarySearch(this.hiddenBlocks, type) >= 0){
                                boolean set = false;
                                CHECKLIGHT: //Check the lighting propagation around the block
                                {
                                    if(player.world.getLightLevel(x + 1, y, z) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x - 1, y, z) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x, y + 1, z) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x, y - 1, z) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x, y, z + 1) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x, y, z - 1) > 0) break CHECKLIGHT;
                                    if(player.world.getLightLevel(x, y, z) > 0) break CHECKLIGHT;
                                    System.out.println(MessageFormat.format("Replaced: Type: {9}, XYZ: {0},{1},{2}, XYZ Start: {3},{4},{5}, XYZ Size: {6},{7},{8}", x,y,z,xPosition,yPosition,zPosition,xSize,ySize,zSize,type));
                                    ((Packet51MapChunk) packet).g[x << 11 | z << 7 | y] = 1;
                                }
                            }
                     }
                 }

             }
             */


               System.out.println(MessageFormat.format("[PLUGIN] Positions: ({0},{1},{2}), Max: ({3},{4},{5}), DataSize: {6}",xPosition,yPosition,zPosition,xSize,ySize,zSize,dataPacket.g.length));
             
            this.breakPacketIntoChunks(xPosition,yPosition,zPosition,xSize,ySize,zSize,dataPacket.g);



             //Recompress the data
             Deflater deflater = new Deflater(-1);
             try {
                    deflater.setInput(dataPacket.g);
                    deflater.finish();
                    deflater.deflate(dataPacket.g);
                } finally {
                    deflater.end();
                }

        }

        this.networkManager.queue(packet);
        //this.g = this.f; //Private, cannot do
    }//sendPacket()
    /**/

    /*
    private void replaceBlockAt (int x, int y, int z, int i, int j, int k, int l, int i1, int j1, byte abyte0[]){
        int k1 = i >> 4;
        int l1 = k >> 4;
        int i2 = (i + l) - 1 >> 4;
        int j2 = (k + j1) - 1 >> 4;
        int k2 = 0;
        int l2 = j;
        int i3 = j + i1;
        if(l2 < 0)
        {
            l2 = 0;
        }
        if(i3 > 128)
        {
            i3 = 128;
        }
        for(int j3 = k1; j3 <= i2; j3++)
        {
            int k3 = i - j3 * 16;
            int l3 = (i + l) - j3 * 16;
            if(k3 < 0)
            {
                k3 = 0;
            }
            if(l3 > 16)
            {
                l3 = 16;
            }
            for(int i4 = l1; i4 <= j2; i4++)
            {
                int j4 = k - i4 * 16;
                int k4 = (k + j1) - i4 * 16;
                if(j4 < 0)
                {
                    j4 = 0;
                }
                if(k4 > 16)
                {
                    k4 = 16;
                }
                //Replace the unlit blocks in this chunk
                //System.out.println("Replacing unlit blocks: " + j3 + "," + i4);
                replaceUnlitBlocks(player.world.getChunkAt(j3, i4),k3, l2, j4, l3, i3, k4, k2,abyte0);
            }
        }
    }
    */

    private int replaceUnlitBlocks(Chunk chunk,int i, int j, int k, int l, int i1, int j1,int k1, byte abyte[]){
                int xMax = l;
                int yMax = i1;
                int zMax = j1;
                int tracker = 0;

                //Create a temporary array that we're going to store our data in
                byte[] newArray;
                if(yMax == 128){ newArray = new byte[(l-i) * (i1-j) * (j1-k)]; }
                else { newArray = new byte[(i1-j)]; }
                for(int x=i; x<xMax || x==i; x++){
                    for(int z=k; z<zMax || z==k; z++){
                        tracker = 0;
                        for(int y=j; y<yMax || y==j; y++){
                            int index = tracker++; //For partial chunk updates, we only loop over the y values in this function
                            if(yMax == 128){ index = (x << 11 | z << 7 | y); } //Use a different index if it's a full chunk update
                            int type = chunk.getTypeId(x,y,z);
                            if(Arrays.binarySearch(this.hiddenBlocks, type) >= 0){
                                boolean set = false;
                                CHECKLIGHT: //Check the lighting propagation around the block
                                {
                                    if(this.getLightLevel(chunk, x + 1, y, z) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x - 1, y, z) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x, y + 1, z) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x, y - 1, z) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x, y, z + 1) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x, y, z - 1) > 0) break CHECKLIGHT;
                                    if(this.getLightLevel(chunk, x, y, z) > 0) break CHECKLIGHT;
                                    //System.out.println(MessageFormat.format("Replaced: Type: {9}, XYZ: {0},{1},{2}, XYZ Start: {3},{4},{5}, XYZ End: {6},{7},{8}", x,y,z,i,j,k,l,i1,j1,type));
                                    newArray[index] = ((byte)(1 & 0xff));
                                    set = true;
                                }
                                if(!set) newArray[index] = ((byte)(type & 0xff));
                            } else {
                                newArray[index] = ((byte)(type & 0xff));
                            }
                        }
                    }
                }
                //Copy our temporary generated array data into the packet data field (abyte)
            System.out.println("[PLUGIN] Abyte size: " + abyte.length + "newArray: " + newArray.length + ", copying to (k1): " + k1);
                System.arraycopy(newArray, 0, abyte, k1, newArray.length);
            //int dataSize = k1 + newArray.length + ((newArray.length / 2)*3);
        int dataSize =  k1 + newArray.length;
            System.out.println("[PLUGIN] Overwitten data size: " + dataSize);
             return dataSize;
    }
    public int getLightLevel(Chunk chunk, int x, int y, int z){
        //We have to use the world.getLight because sometimes the lighting will cross chunks / updates
        //  which leaves us with missing ores and generally cause issues.
        return player.world.getLightLevel((chunk.x << 4) | (x & 0xF), y & 0x7F, (chunk.z << 4) | (z & 0xF));
    }


    //This is done because the arrays are concatinated together inside the packet
    //  we can't directly access data for a given x,y,z because we don't know where in the packet
    //  it is without going through this
    private void breakPacketIntoChunks(int i, int j, int k, int l, int i1, int j1, byte abyte0[]){
        int k1 = i >> 4;
        int l1 = k >> 4;
        int i2 = i + l - 1 >> 4;
        int j2 = k + j1 - 1 >> 4;
        int k2 = 0;
        int l2 = j;
        int i3 = j + i1;

        if(l2 < 0) {
            l2 = 0;
        }

        if(i3 > 128) {
            i3 = 128;
        }

        for (int j3 = k1; j3 <= i2; ++j3) {
            int k3 = i - j3 * 16;
            int l3 = i + l - j3 * 16;
            if(k3 < 0) {
                k3 = 0;
            }
            if(l3 > 16) {
                l3 = 16;
            }
            for(int i4 = l1; i4 <= j2; ++i4) {
                int j4 = k - i4 * 16;
                int k4 = k + j1 - i4 * 16;
                if(j4 < 0) {
                    j4 = 0;
                }
                if(k4 > 16) {
                    k4 = 16;
                }
                //Replace the unlit blocks in this chunk

                Chunk chunk = player.world.getChunkAt(j3, i4);
                //Now, turn this chunk 
                System.out.println(MessageFormat.format("[PLUGIN] Chunk: {0},{1} - Sizes: ({2},{3},{4}) -> ({5},{6},{7}), K2: {8}",
                        j3,i4,k3,l2,j4,l3,i3,k4,k2
                        ));
                if(i1 == 128){
                    k2 = replaceUnlitBlocks(chunk,k3, l2, j4, l3, i3, k4, k2,abyte0);
                } else {
                    for (int subchunkx = k3; subchunkx < l3; ++subchunkx) {
                        for (int subchunkz = j4; subchunkz < k4; ++subchunkz) {
                            k2 = this.replaceUnlitBlocks(chunk,subchunkx,l2,subchunkz,subchunkx,i3,subchunkz,k2,abyte0);
                        }
                    }
                }
            }
        }
    }
}
