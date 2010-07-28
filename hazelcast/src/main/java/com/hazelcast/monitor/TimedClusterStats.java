/*
 * Copyright (c) 2008-2010, Hazel Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.hazelcast.monitor;

import com.hazelcast.core.Member;
import com.hazelcast.impl.MemberImpl;
import com.hazelcast.impl.MemberStatsImpl;
import com.hazelcast.nio.DataSerializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TimedClusterStats implements DataSerializable {
    long time;
    Map<Member, MemberStats> memberStats = new ConcurrentHashMap<Member, MemberStats>();

    public void writeData(DataOutput out) throws IOException {
        out.writeLong(time);
        out.writeInt(memberStats.size());
        Set<Map.Entry<Member, MemberStats>> memberStatEntries = memberStats.entrySet();
        for (Map.Entry<Member, MemberStats> memberStatEntry : memberStatEntries) {
            memberStatEntry.getKey().writeData(out);
            memberStatEntry.getValue().writeData(out);
        }
    }

    public void readData(DataInput in) throws IOException {
        time = in.readLong();
        int memberStatsCount = in.readInt();
        for (int i = 0; i < memberStatsCount; i++) {
            Member member = new MemberImpl();
            member.readData(in);
            MemberStatsImpl memberStatsImpl = new MemberStatsImpl();
            memberStatsImpl.readData(in);
            memberStats.put(member, memberStatsImpl);
        }
    }

    public boolean containsKey(Member member) {
        return memberStats.containsKey(member);
    }

    public void putMemberStats(Member member, MemberStats mapStat) {
        memberStats.put(member, mapStat);
    }

    public Map<Member, MemberStats> getMemberStats() {
        return memberStats;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
