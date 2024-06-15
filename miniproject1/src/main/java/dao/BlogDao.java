package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import service.DBService;
import util.Util;
import vo.CommentVo;
import vo.MemberVo;
import vo.PostVo;
import vo.Post_LikeVo;

public class BlogDao {
	
	static BlogDao single = null;

	public static BlogDao getinstance() {

		if (single == null)
			single = new BlogDao();

		return single;
	}

	private BlogDao() {
	}
	
	// 회원 가입 기능
	public int memberInsert(MemberVo vo) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "insert into member values(seq_member_m_idx,?,?,?,?,?,sysdate,sysdate,?)";	

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, vo.getM_name());
			pstmt.setString(2, vo.getM_id());
			pstmt.setString(3, vo.getM_pw());
			pstmt.setString(4, vo.getM_email());
			pstmt.setString(5, vo.getM_intro());
			pstmt.setInt(6, vo.getM_type());
			
			res = pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 회원 정보 조회
	public List<MemberVo> selectMemberList() {

		List<MemberVo> list = new ArrayList<MemberVo>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "select * from member";

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				MemberVo member = new MemberVo();
	            member.setM_idx(rs.getInt("m_idx"));
	            member.setM_name(rs.getString("m_name"));
	            member.setM_id(rs.getString("m_id"));
	            member.setM_pw(rs.getString("m_pw"));
	            member.setM_email(rs.getString("m_email"));
	            member.setM_intro(rs.getString("m_intro"));
	            member.setM_rdate(rs.getString("m_rdate"));
	            member.setM_mdate(rs.getString("m_mdate"));
	            member.setM_type(rs.getInt("m_type"));

	            list.add(member);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		return list;
	}
	
	// 회원 정보 수정 (비밀번호, 아이디 제외)
	public int memberUpdate(MemberVo vo) {
		
		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String sql = null;
		
		try {
			sql = "update posts set m_name = ?, m_email = ?, m_intro = ? where m_idx = ?";
			
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, vo.getM_name());
            pstmt.setString(2, vo.getM_email());
            pstmt.setString(3, vo.getM_intro());
            pstmt.setInt(4, vo.getM_idx());
            
			res = pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 회원 탈퇴
	public int memberDelete(int m_idx) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "delete from member where m_idx = ?";

		try {
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1,m_idx);

			res = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 로그인 기능
	// remember me 옵션을 선택할 시 쿠키를 생성하여 로그인 상태 유지
	public boolean login(String m_id, String m_pw, String remember, HttpServletRequest req, HttpServletResponse resp) {
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		boolean status = false;
		
		try {
			String sql = "select * from member where m_id = ? and m_pw ?";
			
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setString(1, m_id);
			pstmt.setString(2, Util.MD5(m_pw));
			rs = pstmt.executeQuery();
			status = rs.next();
			// 로그인 성공 했을 경우
			if(status) {
				// 세션 생성
				int m_idx = rs.getInt("m_idx");
				String m_name = rs.getString("m_name");
				// 세션에 m_idx와 m_name 추가
				req.getSession().setAttribute("m_idx", m_idx);
	            req.getSession().setAttribute("m_name", m_name);
	            
	            // 쿠키 생성
	            if (remember != null) {
	            	m_name = m_name.replaceAll(" ", "_");
	            	String vlaue = m_idx + "_" + m_name;
	            	// user라는 쿠키 추가
	            	Cookie cookie = new Cookie("user", vlaue);
	            	// 쿠키 만료 시간
	                cookie.setMaxAge(60 * 60 * 30);
	                // 쿠키 응답 추가
	                resp.addCookie(cookie);
	            }
			}
		} catch (SQLException e) {
			System.err.print("Login Error : " + e);
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
		return status;
	} 
	
	// 이메일을 통한 아이디 찾기
	public String getId(String m_email) {
        String id = "";
        
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

        try {
            String sql = "select m_id from member where m_email=?";
            
            conn = DBService.getinstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, m_email);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                id = rs.getString("m_id");
            }
        } catch (Exception e) {
            System.err.println("getId error: " + e);
        } finally {
        	try {
        		if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
        }
        return id;
    }
	
	// 비밀번호 변경 ( -1 = 업데이트가 이루어지지 않았을때, -2 = 새 비밀번호가 일치하지 않았을때 )
	//                               예전 비밀번호          새 비밀번호            새 비밀번호 확인용
    public int changePassword(String oldPassword, String newPassword, String newPasswordr, int m_idx) {
        int res = 0;
        
        Connection conn = null;
		PreparedStatement pstmt = null;
        
		// 새 비밀번호랑 확인용이 같다면
        if (newPassword.equals(newPasswordr)) {
            try {
                String sql = "update member set m_pw = ?  where m_pw = ? and m_idx = ? ";
                
                conn = DBService.getinstance().getConnection();
                pstmt = conn.prepareStatement(sql);
                
                pstmt.setString(1, Util.MD5(newPassword));
                pstmt.setString(2, Util.MD5(oldPassword));
                pstmt.setInt(3, m_idx);
                res = pstmt.executeUpdate();
                if (res == 0) {
                	// 업데이트가 이루어지지 않았을 때
                	res = -1;
                }
            } catch (Exception e) {
                System.err.println("changePassword Error : " + e);
            } finally {
            	try {
    				if (pstmt != null)
    					pstmt.close();
    				if (conn != null)
    					conn.close();
    			} catch (SQLException e) {
    				e.printStackTrace();
    			}
            }

        } else {
        	// 새로운 비밀번호들이 일치하지 않을 때
        	res = -2;
        }

        return res;
    }
	
	// 게시글 검색
    public List<PostVo> selectPostList() {

		List<PostVo> list = new ArrayList<PostVo>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		String sql = "select * from post";

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				PostVo post = new PostVo();
	            post.setP_idx(rs.getInt("p_idx"));
	            post.setP_cate(rs.getString("p_cate"));
	            post.setP_title(rs.getString("p_title"));
	            post.setP_content(rs.getString("p_content"));
	            post.setP_rdate(rs.getString("p_rdate"));
	            post.setP_mdate(rs.getString("p_mdate"));
	            post.setP_type(rs.getInt("p_type"));
	            post.setP_hit(rs.getInt("p_hit"));
	            post.setM_idx(rs.getInt("m_idx"));

	            list.add(post);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				if (rs != null)
					rs.close();
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}

		return list;
	}
    
    // 게시글 조회 + 페이징			보여줄 페이지의 번호		각 페이지에 표시할 게시글의 개수
    public List<PostVo> selectPostList(int pageNo, int pageSize) {
    	
        List<PostVo> list = new ArrayList<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // 페이징 계산
        int start = (pageNo - 1) * pageSize + 1;
        int end = start + pageSize - 1;

        // 메인 쿼리 : 메인 쿼리에서 rownum을 이용해 조회 범위를 지정, 서브 쿼리 : rownum을 이용해 각 게시글에 번호를 붙임
		// rownum을 이용하여 페이징을 처리 																	
        String sql = "select * from (select rownum rnum, p_idx, p_cate, p_title, p_content, p_rdate, p_mdate, p_type, p_hit, m_idx from (select * from post order by p_idx desc)) where rnum between ? and ?";

        try {
            conn = DBService.getinstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, start);
            pstmt.setInt(2, end);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                PostVo post = new PostVo();
                post.setP_idx(rs.getInt("p_idx"));
                post.setP_cate(rs.getString("p_cate"));
                post.setP_title(rs.getString("p_title"));
                post.setP_content(rs.getString("p_content"));
                post.setP_rdate(rs.getString("p_rdate"));
                post.setP_mdate(rs.getString("p_mdate"));
                post.setP_type(rs.getInt("p_type"));
                post.setP_hit(rs.getInt("p_hit"));
                post.setM_idx(rs.getInt("m_idx"));

                list.add(post);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return list;
    }
    
    // 특정 게시글 조회 (m_idx를 이용한 조회)
    public PostVo selectPostByPidx(int m_idx) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        PostVo post = null;

        String sql = "select * from post where m_idx = ?";

        try {
            conn = DBService.getinstance().getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, m_idx);
            rs = pstmt.executeQuery();

            if (rs.next()) {
            	post = new PostVo();
                post.setP_idx(rs.getInt("p_idx"));
                post.setP_cate(rs.getString("p_cate"));
                post.setP_title(rs.getString("p_title"));
                post.setP_content(rs.getString("p_content"));
                post.setP_rdate(rs.getString("p_rdate"));
                post.setP_mdate(rs.getString("p_mdate"));
                post.setP_type(rs.getInt("p_type"));
                post.setP_hit(rs.getInt("p_hit"));
                post.setM_idx(rs.getInt("m_idx"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return post;
    }
	
	// 게시글 등록
	public int postInsert(PostVo vo) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "insert into post values(seq_post_p_idx,?,?,?,?,?,?,?,?)";	

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			// XSS 방어를 위해 이스케이프 처리
            String p_cate = Util.escapeHtml(vo.getP_cate());
            String p_title = Util.escapeHtml(vo.getP_title());
            String p_content = Util.escapeHtml(vo.getP_content());
			
			pstmt.setString(1, p_cate);
			pstmt.setString(2, p_title);
			pstmt.setString(3, p_content);
			pstmt.setString(4, vo.getP_rdate());
			pstmt.setString(5, vo.getP_mdate());
			pstmt.setInt(6, vo.getP_type());
			pstmt.setInt(7, vo.getP_hit());
			pstmt.setInt(8, vo.getM_idx());
			
			res = pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 게시글 수정
	public int postUpdate(PostVo vo) {
		
		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String sql = null;
		
		try {
			sql = "update posts set p_cate = ?, p_title = ?, p_content = ?, p_mdate = ? where p_idx = ?";
			
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			// XSS 방어를 위해 이스케이프 처리
            String p_cate = Util.escapeHtml(vo.getP_cate());
            String p_title = Util.escapeHtml(vo.getP_title());
            String p_content = Util.escapeHtml(vo.getP_content());
			
			pstmt.setString(1, p_cate);
            pstmt.setString(2, p_title);
            pstmt.setString(3, p_content);
            pstmt.setString(4, vo.getP_mdate());
            pstmt.setInt(5, vo.getP_idx());
            
			res = pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 게시글 삭제
	public int postDelete(int p_idx) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "delete from post where p_idx = ?";

		try {
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1,p_idx);

			res = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 모든 댓글 조회
	public List<CommentVo> selectCommentlist() {

	    List<CommentVo> list = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "select * from comment";

	    try {
	        conn = DBService.getinstance().getConnection();
	        pstmt = conn.prepareStatement(sql);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            CommentVo cv = new CommentVo();
	            cv.setC_idx(rs.getInt("c_idx"));
	            cv.setC_content(rs.getString("c_content"));
	            cv.setC_rdate(rs.getString("c_rdate"));
	            cv.setC_mdate(rs.getString("c_mdate"));
	            cv.setP_idx(rs.getInt("p_idx"));
	            cv.setM_idx(rs.getInt("m_idx"));

	            list.add(cv);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    return list;
	}
	
	// 게시글에 달린 댓글 목록 조회
	public List<CommentVo> selectCommentByPidx(int p_idx) {

	    List<CommentVo> list = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "select * from comment where p_idx = ?";

	    try {
	        conn = DBService.getinstance().getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, p_idx);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            CommentVo cv = new CommentVo();
	            cv.setC_idx(rs.getInt("c_idx"));
	            cv.setC_content(rs.getString("c_content"));
	            cv.setC_rdate(rs.getString("c_rdate"));
	            cv.setC_mdate(rs.getString("c_mdate"));
	            cv.setP_idx(rs.getInt("p_idx"));
	            cv.setM_idx(rs.getInt("m_idx"));

	            list.add(cv);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    return list;
	}
	
	//특정 댓글 조회 (m_idx를 이용한 조회)
	public List<CommentVo> selectCommentByMidx(int m_idx) {

	    List<CommentVo> list = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "select * from comment where m_idx = ?";

	    try {
	        conn = DBService.getinstance().getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, m_idx);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            CommentVo cv = new CommentVo();
	            cv.setC_idx(rs.getInt("c_idx"));
	            cv.setC_content(rs.getString("c_content"));
	            cv.setC_rdate(rs.getString("c_rdate"));
	            cv.setC_mdate(rs.getString("c_mdate"));
	            cv.setP_idx(rs.getInt("p_idx"));
	            cv.setM_idx(rs.getInt("m_idx"));

	            list.add(cv);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    return list;
	}
	
	// 댓글 등록
	public int commentInsert(CommentVo vo) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "insert into comment values(seq_comment_c_idx,?,sysdate,sysdate,?,?)";	

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			// XSS 방어를 위해 이스케이프 처리
            String c_content = Util.escapeHtml(vo.getC_content());
			
			pstmt.setString(1, c_content);
			pstmt.setInt(2, vo.getP_idx());
			pstmt.setInt(3, vo.getM_idx());
			
			res = pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 댓글 수정
	public int commentUpdate(CommentVo vo) {
		
		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		String sql = null;
		
		try {
			sql = "update comment set c_content = ?, c_mdate = ? where c_idx = ?";
			
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);
			
			// XSS 방어를 위해 이스케이프 처리
            String c_content = Util.escapeHtml(vo.getC_content());
			
			pstmt.setString(1, c_content);
            pstmt.setString(2, vo.getC_mdate());
            pstmt.setInt(3, vo.getC_idx());;
            
			res = pstmt.executeUpdate();
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 댓글 삭제
	public int commentDelete(int c_idx) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "delete from comment where c_idx = ?";

		try {
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1,c_idx);

			res = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 좋아요 또는 스크랩 등록
	public int insertPostLike(Post_LikeVo vo) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "insert into post_like values (seq_post_like_l_idx.nextval, ?, sysdate, sysdate, ?, ?)";	

		try {

			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, vo.getL_type());
			pstmt.setInt(2, vo.getM_idx());
			pstmt.setInt(3, vo.getP_idx());
			
			res = pstmt.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 좋아요 또는 스크랩 취소
	public int postLikeDelete(int l_idx) {

		int res = 0;
		Connection conn = null;
		PreparedStatement pstmt = null;

		String sql = "delete from comment where l_idx = ?";

		try {
			conn = DBService.getinstance().getConnection();
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1,l_idx);

			res = pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (pstmt != null)
					pstmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return res;
	}
	
	// 특정 게시글에 대한 사용자의 좋아요 또는 스크랩 여부 확인
	public boolean isPostLikeOrScrap(int m_idx, int p_idx, int l_type) {
		
	    boolean likeOrScrap = false;
	    
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "select * from post_like where m_idx = ? AND p_idx = ? AND l_type = ?";

	    try {
	        conn = DBService.getinstance().getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, m_idx);
	        pstmt.setInt(2, p_idx);
	        pstmt.setInt(3, l_type);
	        rs = pstmt.executeQuery();

	        // 해당 게시글에 대해 좋아요 또는 스크랩을 한 경우
	        if (rs.next()) {
	        	likeOrScrap = true; 
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    return likeOrScrap;
	}
	
	// 사용자가 게시글에 대해 좋아요 또는 스크랩한 목록 확인
	public boolean isUserLikeOrScrap(int m_idx, int l_type) {
		
	    boolean likeOrScrap = false;
	    
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    String sql = "select * from post_like where m_idx = ? and l_type = ?";

	    try {
	        conn = DBService.getinstance().getConnection();
	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, m_idx);
	        pstmt.setInt(2, l_type);
	        rs = pstmt.executeQuery();
	        
	        // 해당 게시글에 대해 좋아요 또는 스크랩을 한 경우
	        if (rs.next()) {
	        	likeOrScrap = true;
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (pstmt != null) pstmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    return likeOrScrap;
	}
	
}
