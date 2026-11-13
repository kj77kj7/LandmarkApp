import React, { useState, useEffect, useCallback, useRef } from 'react';
import { 
  StyleSheet, Text, View, Image, TouchableOpacity, ScrollView, RefreshControl, 
  Alert, Platform, Animated, Dimensions, FlatList, ActivityIndicator 
} from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { getItem, deleteItem } from '../utils/authStorage'; 
import { API_URL } from '../constants/constants';

const { width } = Dimensions.get('window');
const DRAWER_WIDTH = Math.min(width * 0.85, 320);

export default function HomeScreen({ navigation, stepCount, onLogout }) {
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  
  // 유저 정보 State
  const [todayDate, setTodayDate] = useState("");
  const [nickname, setNickname] = useState("");
  const [serverTotalSteps, setServerTotalSteps] = useState(0);
  const [myRank, setMyRank] = useState(0);
  const [targetLandmark, setTargetLandmark] = useState("");
  const [goalSteps, setGoalSteps] = useState(0);
  const [currentLocation, setCurrentLocation] = useState("");
  // 프로필 이미지 상태
  const [profileImageUrl, setProfileImageUrl] = useState(null);

  // 알림 관련 State
  const [isNotiVisible, setIsNotiVisible] = useState(false);
  const slideAnim = useRef(new Animated.Value(DRAWER_WIDTH)).current;
  const [notifications, setNotifications] = useState([]);
  const [notiPage, setNotiPage] = useState(0);
  const [notiHasNext, setNotiHasNext] = useState(true);
  const [notiLoading, setNotiLoading] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);

  const displayTotalSteps = serverTotalSteps + (stepCount || 0);
  const remainingSteps = Math.max(0, goalSteps - displayTotalSteps);

  useEffect(() => {
    const now = new Date();
    setTodayDate(`${now.getMonth() + 1}월 ${now.getDate()}일`);
    fetchHomeData();
    fetchUnreadCount(); 
  }, []);

  useFocusEffect(
    useCallback(() => {
      fetchHomeData();
      fetchUnreadCount(); 
    }, [])
  );

  // === 홈 데이터 로드 ===
  const fetchHomeData = async () => {
    try {
      const token = await getItem('userToken');
      if (!token) {
        if(onLogout) onLogout(); 
        return;
      }

      const headers = { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` };

      // 1. 홈 데이터 조회
      const homeRes = await fetch(`${API_URL}/api/home`, { method: 'GET', headers });
      
      if (homeRes.status === 401) {
        Alert.alert("인증 만료", "다시 로그인 해주세요.");
        await deleteItem('userToken');
        if(onLogout) onLogout();
        return;
      }
      const homeJson = await homeRes.json();

      // 내 정보 조회 (프로필 이미지 가져오기 위함)
      // 홈 데이터 API에 프로필 이미지가 없다면 별도 호출 필요
      const userRes = await fetch(`${API_URL}/api/user/info`, { headers });
      const userJson = await userRes.json();

      if (homeJson.success) {
        const homeData = homeJson.data;
        setNickname(homeData.userInfo.nickname);
        setServerTotalSteps(homeData.userInfo.totalSteps);
        setMyRank(homeData.rankingInfo.rank);
        setTargetLandmark(homeData.landmarkInfo.name);
        setGoalSteps(homeData.landmarkInfo.requiredSteps);
        setCurrentLocation(homeData.currentLocationName);
      }

      if (userJson.success) {
        // 프로필 이미지 설정
        setProfileImageUrl(userJson.data.profileImageUrl);
      }

    } catch (error) {
      console.error("통신 에러:", error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  // === 알림 개수 조회 ===
  const fetchUnreadCount = async () => {
    try {
      const token = await getItem('userToken');
      if (!token) return;
      
      const response = await fetch(`${API_URL}/api/notifications/unread-count`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const json = await response.json();
      if (json.success) {
        setUnreadCount(json.data);
      }
    } catch (e) { console.error("알림 개수 로드 실패", e); }
  };

  // === 개발용: 걸음 수 25,000보 추가 (개발 빌드에서만 버튼 표시) ===
  const addTestSteps = async () => {
    try {
      const token = await getItem('userToken');
      await fetch(`${API_URL}/api/steps/sync`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ steps: 25000 }) 
      });
      
      if(Platform.OS === 'web') alert("개발용: 25,000보 추가됨");
      else Alert.alert("개발용", "25,000보가 추가되었습니다.");
      
      fetchHomeData(); 
    } catch (e) { console.error(e); }
  };

  // === 알림 목록 로드 ===
  const fetchNotifications = async (page = 0, reset = false) => {
    if (notiLoading) return;
    setNotiLoading(true);

    try {
      const token = await getItem('userToken');
      const response = await fetch(`${API_URL}/api/notifications?page=${page}&size=10`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      const json = await response.json();

      if (json.success) {
        const sliceData = json.data;
        const newItems = sliceData.content;
        
        if (reset) {
            setNotifications(newItems);
        } else {
            setNotifications(prev => [...prev, ...newItems]);
        }
        
        setNotiPage(page);
        setNotiHasNext(!sliceData.last);
      }
    } catch (error) {
      console.error("알림 로드 실패:", error);
    } finally {
      setNotiLoading(false);
    }
  };

  // === 알림창 토글 ===
  const toggleNotification = () => {
    if (isNotiVisible) {
      Animated.timing(slideAnim, {
        toValue: DRAWER_WIDTH,
        duration: 300,
        useNativeDriver: true,
      }).start(() => setIsNotiVisible(false));
      fetchUnreadCount();
    } else {
      setIsNotiVisible(true);
      fetchNotifications(0, true);
      Animated.timing(slideAnim, {
        toValue: 0,
        duration: 300,
        useNativeDriver: true,
      }).start();
    }
  };

  // === 알림 삭제 ===
  const handleDeleteNotification = async (notiId) => {
    try {
        const token = await getItem('userToken');
        const res = await fetch(`${API_URL}/api/notifications/${notiId}`, {
            method: 'DELETE',
            headers: { Authorization: `Bearer ${token}` }
        });
        const json = await res.json();
        
        if (json.success) {
            setNotifications(prev => prev.filter(n => n.id !== notiId));
            setUnreadCount(prev => Math.max(0, prev - 1));
        }
    } catch (e) { console.error("알림 삭제 실패", e); }
  };

  // === 친구 수락/거절 ===
  const handleAccept = async (notiId) => {
    try {
        const token = await getItem('userToken');
        const res = await fetch(`${API_URL}/api/notifications/${notiId}/accept`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` }
        });
        const json = await res.json();
        if (json.success) {
            Alert.alert("수락 완료", "친구가 되었습니다!");
            setNotifications(prev => prev.filter(n => n.id !== notiId));
            setUnreadCount(prev => Math.max(0, prev - 1));
        } else {
            Alert.alert("실패", json.message);
        }
    } catch (e) { console.error(e); }
  };

  const handleReject = async (notiId) => {
    try {
        const token = await getItem('userToken');
        const res = await fetch(`${API_URL}/api/notifications/${notiId}/reject`, {
            method: 'POST',
            headers: { Authorization: `Bearer ${token}` }
        });
        const json = await res.json();
        if (json.success) {
            setNotifications(prev => prev.filter(n => n.id !== notiId));
            setUnreadCount(prev => Math.max(0, prev - 1));
        } else {
            Alert.alert("실패", json.message);
        }
    } catch (e) { console.error(e); }
  };

  const formatTimeAgo = (dateString) => {
    const now = new Date();
    const past = new Date(dateString);
    const diffMS = now - past;
    const diffMins = Math.floor(diffMS / (1000 * 60));
    const diffHours = Math.floor(diffMS / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMS / (1000 * 60 * 60 * 24));

    if (diffDays > 0) return `${diffDays}일 전`;
    if (diffHours > 0) return `${diffHours}시간 전`;
    if (diffMins > 0) return `${diffMins}분 전`;
    return "방금 전";
  };

  const renderNotificationItem = ({ item }) => {
    const isFriendRequest = item.type === 'FRIEND_REQUEST';
    const isFriendAccept = item.type === 'FRIEND_ACCEPT';
    const isFriendRelated = isFriendRequest || isFriendAccept;
    
    return (
      <View style={styles.notiItemContainer}>
        <TouchableOpacity 
            style={styles.deleteNotiBtn} 
            onPress={() => handleDeleteNotification(item.id)}
            hitSlop={{top: 10, bottom: 10, left: 10, right: 10}}
        >
            <Text style={styles.deleteNotiText}>✕</Text>
        </TouchableOpacity>

        <View style={styles.notiTopRow}>
            <View style={styles.notiIconBox}>
                {isFriendRelated ? (
                    <Image 
                        source={{ uri: item.senderProfileImage || "https://cdn-icons-png.flaticon.com/512/847/847969.png" }} 
                        style={styles.notiProfileImg} 
                    />
                ) : (
                    <Text style={{fontSize: 20}}>🎉</Text>
                )}
            </View>

            <View style={styles.notiContentBox}>
                {isFriendRequest ? (
                    <Text style={styles.notiText} numberOfLines={2}>
                        <Text style={{fontWeight:'bold'}}>{item.senderName}</Text>
                        <Text>님이 친구 요청을 보냈습니다.</Text>
                    </Text>
                ) : (
                    <Text style={styles.notiText}>{item.message}</Text>
                )}
            </View>
        </View>

        {isFriendRequest && (
            <View style={styles.notiBtnRow}>
                <TouchableOpacity style={styles.acceptBtn} onPress={() => handleAccept(item.id)}>
                    <Text style={styles.btnTextWhite}>수락</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.rejectBtn} onPress={() => handleReject(item.id)}>
                    <Text style={styles.btnTextGray}>거절</Text>
                </TouchableOpacity>
            </View>
        )}

        <View style={styles.notiFooter}>
             <Text style={styles.timeText}>{formatTimeAgo(item.createdAt)}</Text>
        </View>
        
        <View style={styles.divider} />
      </View>
    );
  };

  const onRefresh = () => {
    setRefreshing(true);
    fetchHomeData();
    fetchUnreadCount();
  };

  const handleLogoutPress = () => {
    if (Platform.OS === 'web') {
      const isConfirmed = window.confirm("정말 로그아웃 하시겠습니까?");
      if (isConfirmed) {
        deleteItem('userToken').then(() => { if (onLogout) onLogout(); });
      }
      return;
    }
    Alert.alert(
      "로그아웃", "정말 로그아웃 하시겠습니까?",
      [{ text: "취소", style: "cancel" }, { text: "확인", style: 'destructive', onPress: async () => { await deleteItem('userToken'); if (onLogout) onLogout(); } }]
    );
  };

  if (loading) {
    return (
      <View style={[styles.container, styles.center]}>
        <ActivityIndicator size="large" color="#4A90E2" />
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <ScrollView 
        contentContainerStyle={styles.scrollContent}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      >
        <View style={styles.headerSection}>
          <View style={{flex: 1}}>
            <Text style={styles.dateText}>{todayDate}</Text>
            <Text style={styles.greetingText}>오늘 {nickname}님의 걸음 수예요</Text>
            <View style={styles.stepsWrapper}>
              <Text style={styles.stepsPrefix}>총</Text>
              <Text style={styles.stepsCount}>{displayTotalSteps.toLocaleString()} 걸음</Text>
            </View>
            <Text style={styles.goalText}>
               {remainingSteps > 0 
                 ? `${targetLandmark} 달성까지 ${remainingSteps.toLocaleString()}보 남았어요`
                 : `축하합니다! ${targetLandmark}에 도착했어요! 🎉`}
            </Text>
          </View>
          
          <View style={{alignItems: 'flex-end'}}>
             <TouchableOpacity onPress={toggleNotification} style={styles.bellButton}>
                <Image 
                    source={{uri: "https://cdn-icons-png.flaticon.com/512/3602/3602145.png"}} 
                    style={{width: 24, height: 24, tintColor: '#888'}} 
                />
                {unreadCount > 0 && (
                    <View style={styles.badge}>
                        <Text style={styles.badgeText}>
                            {unreadCount > 99 ? '99+' : unreadCount}
                        </Text>
                    </View>
                )}
             </TouchableOpacity>

             <View style={styles.penguinImageContainer}>
                {/* 프로필 이미지가 있으면 그것을, 없으면 기본 아이콘 사용 */}
                <Image 
                    source={profileImageUrl ? { uri: profileImageUrl } : require('../../assets/images/HomeIcon.png')} 
                    style={styles.penguinImage} 
                />
             </View>
          </View>
        </View>

        {/* 개발용 버튼: 배포 빌드(__DEV__ = false)에서는 보이지 않음 */}
        {__DEV__ && (
          <TouchableOpacity
              style={{backgroundColor: '#FFEB3B', padding: 12, borderRadius: 10, marginBottom: 20, alignItems:'center', borderWidth:1, borderColor:'#FBC02D'}}
              onPress={addTestSteps}
          >
              <Text style={{fontWeight: 'bold', color: '#333'}}>개발용: 걸음 수 25,000보 추가</Text>
          </TouchableOpacity>
        )}

        <View style={styles.menuCard}>
          <MenuItem 
            title="랜드마크" desc={`현재: ${currentLocation}`} iconPlaceholder="🏯" isLandmark={true}
            onClick={() => navigation.navigate('Landmark')} 
          />
          <MenuItem 
            title="랭킹" desc={myRank > 0 ? `${myRank}위` : "-"} iconPlaceholder="🥈"
            onClick={() => navigation.navigate('Ranking')} 
          />
          
          {/* 업적 버튼 연결 */}
          <MenuItem 
            title="업적" desc="" iconPlaceholder="🏆"
            onClick={() => navigation.navigate('Achievement')} 
          />
        </View>

        <BottomButton title="내 프로필" iconPlaceholder="👤" onClick={() => navigation.navigate('Profile')} />
        <BottomButton title="친구" iconPlaceholder="👥" onClick={() => navigation.navigate('FriendList')} />
        
        <TouchableOpacity style={[styles.bottomButton, styles.logoutButton]} onPress={handleLogoutPress}>
           <View style={[styles.iconBox, styles.logoutIconBox]}>
             <Text style={{fontSize: 20}}>🚪</Text>
           </View>
           <Text style={[styles.menuTitle, {color: '#d32f2f'}]}>로그아웃</Text>
        </TouchableOpacity>
      </ScrollView>

      {/* 알림창 슬라이드 */}
      {isNotiVisible && (
        <View style={styles.drawerOverlay}>
            <TouchableOpacity style={styles.drawerBackdrop} onPress={toggleNotification} activeOpacity={1} />
            
            <Animated.View style={[styles.drawerContainer, { transform: [{ translateX: slideAnim }] }]}>
                <View style={styles.drawerHeader}>
                    <Text style={styles.drawerTitle}>알림</Text>
                    <TouchableOpacity onPress={toggleNotification} style={{padding: 5}}>
                        <Text style={{fontSize: 18, color:'#999'}}>✕</Text>
                    </TouchableOpacity>
                </View>
                
                <View style={{width: '100%', height: 1, backgroundColor: '#f0f0f0'}} />

                <FlatList
                    data={notifications}
                    renderItem={renderNotificationItem}
                    keyExtractor={(item) => item.id.toString()}
                    contentContainerStyle={{padding: 20}}
                    ListEmptyComponent={
                        <View style={{alignItems:'center', marginTop: 100}}>
                            <Text style={{fontSize: 40, marginBottom: 10}}>📭</Text>
                            <Text style={{color:'#aaa'}}>새로운 알림이 없습니다.</Text>
                        </View>
                    }
                    onEndReached={() => {
                        if (notiHasNext) fetchNotifications(notiPage + 1);
                    }}
                    onEndReachedThreshold={0.5}
                    ListFooterComponent={notiLoading ? <ActivityIndicator size="small" color="#aaa" style={{marginTop: 10}} /> : null}
                />
            </Animated.View>
        </View>
      )}
    </View>
  );
}

function MenuItem({ title, desc, iconPlaceholder, iconSource, isLandmark, onClick }) {
  const shouldHideDesc = !desc; 
  return (
    <TouchableOpacity style={styles.menuItem} onPress={onClick} activeOpacity={0.7}>
      <View style={styles.iconBox}>
        {iconSource ? (
           <Image source={iconSource} style={[styles.iconImage, isLandmark && styles.landmarkTransform]} />
        ) : (
           <Text style={[styles.iconText, isLandmark && styles.landmarkTransform]}>{iconPlaceholder}</Text>
        )}
      </View>
      <View style={styles.textBox}>
        <Text style={styles.menuTitle}>{title}</Text>
        {!shouldHideDesc ? (
           <Text style={styles.menuDesc}>{desc}</Text>
        ) : (
           title !== "업적" && <Text style={styles.noDataDesc}>기록 없음</Text>
        )}
      </View>
    </TouchableOpacity>
  );
}

function BottomButton({ title, iconPlaceholder, iconSource, onClick }) {
  return (
    <TouchableOpacity style={styles.bottomButton} onPress={onClick} activeOpacity={0.8}>
      <View style={styles.iconBox}>
        {iconSource ? (
           <Image source={iconSource} style={styles.iconImage} />
        ) : (
           <Text style={styles.iconText}>{iconPlaceholder}</Text>
        )}
      </View>
      <Text style={styles.menuTitle}>{title}</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#F7FAFF' },
  scrollContent: { padding: 24, paddingTop: 10 },
  center: { justifyContent: 'center', alignItems: 'center' },
  headerSection: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 30, marginTop: 20 },
  dateText: { fontSize: 15, color: '#515151', marginBottom: 16, fontWeight: '300' },
  greetingText: { fontSize: 15, color: '#6D6D6D', marginBottom: 2, fontWeight: '300' },
  stepsWrapper: { flexDirection: 'row', alignItems: 'baseline', marginBottom: 4 },
  stepsPrefix: { fontSize: 20, marginRight: 5, color: '#3584FE', fontWeight: '600' },
  stepsCount: { fontSize: 20, fontWeight: '700', color: '#3584FE' },
  goalText: { fontSize: 12, color: '#6D6D6D', fontWeight: '300' },
  penguinImageContainer: { width: 103, height: 100, justifyContent: 'center', alignItems: 'center', borderRadius: 50, overflow: 'hidden' },
  penguinImage: { width: '100%', height: '100%', resizeMode: 'cover' },
  bellButton: { padding: 10, marginBottom: 10, alignItems: 'flex-end', position: 'relative' },
  badge: {
    position: 'absolute', top: 5, right: 5, 
    backgroundColor: '#FF3B30', borderRadius: 10, 
    minWidth: 20, height: 20, 
    justifyContent: 'center', alignItems: 'center', 
    paddingHorizontal: 4, borderWidth: 1.5, borderColor: '#fff'
  },
  badgeText: { color: 'white', fontSize: 10, fontWeight: 'bold' },
  menuCard: { backgroundColor: 'white', borderRadius: 28, paddingVertical: 10, marginBottom: 20, borderWidth: 0.3, borderColor: '#D9D9D9', shadowColor: '#000', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.05, shadowRadius: 10, elevation: 3 },
  menuItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 15, paddingHorizontal: 24, borderBottomWidth: 1, borderBottomColor: '#EEEEEE' },
  iconBox: { width: 45, height: 45, backgroundColor: '#F7FAFF', borderRadius: 50, justifyContent: 'center', alignItems: 'center', marginRight: 16 },
  iconText: { fontSize: 20 },
  iconImage: { width: '60%', height: '60%', resizeMode: 'contain' },
  landmarkTransform: { transform: [{ scale: 1.4 }] },
  textBox: { flex: 1, flexDirection: 'column' },
  menuTitle: { fontSize: 15, fontWeight: '600', color: '#515151', lineHeight: 20 },
  menuDesc: { fontSize: 12, color: '#000000', marginTop: 2, fontWeight: '300' },
  noDataDesc: { color: '#ccc', fontSize: 11, marginTop: 2 },
  bottomButton: { backgroundColor: 'white', borderRadius: 25, paddingHorizontal: 24, height: 60, marginBottom: 12, flexDirection: 'row', alignItems: 'center', borderWidth: 0.3, borderColor: '#D9D9D9' },
  logoutButton: { backgroundColor: '#ffebee', marginTop: 10, borderColor: '#ffcdd2' },
  logoutIconBox: { backgroundColor: '#ffffff' },
  drawerOverlay: {
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    zIndex: 2000, flexDirection: 'row', justifyContent: 'flex-end',
  },
  drawerBackdrop: { 
    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: 'rgba(0,0,0,0.4)' 
  },
  drawerContainer: {
    width: DRAWER_WIDTH, 
    height: '100%', 
    backgroundColor: 'white',
    paddingTop: Platform.OS === 'ios' ? 50 : 20,
    borderTopLeftRadius: 20,
    borderBottomLeftRadius: 20,
    shadowColor: "#000", shadowOffset: { width: -5, height: 0 }, shadowOpacity: 0.1, shadowRadius: 10, elevation: 15
  },
  drawerHeader: {
      flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
      paddingVertical: 15, paddingHorizontal: 20
  },
  drawerTitle: { fontSize: 18, fontWeight: 'bold', color: '#333' },
  notiItemContainer: { marginBottom: 15, backgroundColor: '#fff', position: 'relative' },
  deleteNotiBtn: {
      position: 'absolute', top: 0, right: 0, 
      zIndex: 10, padding: 5
  },
  deleteNotiText: { fontSize: 16, color: '#aaa', fontWeight: 'bold' },
  notiTopRow: { 
      flexDirection: 'row', alignItems: 'flex-start',
      justifyContent: 'space-between',
      paddingRight: 20
  },
  notiIconBox: { 
      width: 40, height: 40, borderRadius: 20, backgroundColor: '#f9f9f9', 
      justifyContent: 'center', alignItems: 'center', marginRight: 12, overflow: 'hidden'
  },
  notiProfileImg: { width: '100%', height: '100%' },
  notiContentBox: { 
      flex: 1, justifyContent: 'center', minHeight: 40,
      flexShrink: 1 
  },
  notiText: { fontSize: 14, color: '#333', lineHeight: 20 },
  notiBtnRow: { 
      flexDirection: 'row', alignItems: 'center', marginTop: 8, marginLeft: 52 
  },
  acceptBtn: { 
      backgroundColor: '#4A90E2', paddingHorizontal: 12, paddingVertical: 8, 
      borderRadius: 8, marginRight: 8 
  },
  rejectBtn: { 
      backgroundColor: '#f0f0f0', paddingHorizontal: 12, paddingVertical: 8, 
      borderRadius: 8 
  },
  btnTextWhite: { fontSize: 12, color: '#fff', fontWeight: 'bold' },
  btnTextGray: { fontSize: 12, color: '#555' },
  notiFooter: { flexDirection: 'row', justifyContent: 'flex-end', marginTop: 4 },
  timeText: { fontSize: 11, color: '#ccc' },
  divider: { width: '100%', height: 1, backgroundColor: '#f0f0f0', marginTop: 15 }
});