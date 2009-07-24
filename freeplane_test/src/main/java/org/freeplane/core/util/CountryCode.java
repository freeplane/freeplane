package org.freeplane.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * Supported languages
 * 
 * @author Robert Ladstaetter
 *
 * @since 21.07.2009
 */
public enum CountryCode {
	ar, ca, cs, da, de, el, es, et, fr, gl, hr, hu, id, it, ja, kr, lt, nb, nl, nn, pl, pt_BR, pt_PT, ru, se, sk, sl, tr, uk_UA, vi, zh_CN, zh_TW,
	// unsupported	
	af, ax, al, dz, as, ad, ao, ai, aq, ag, am, aw, au, at, az, bs, bh, bd, bb, by, be, bz, bj, bm, bt, bo, ba, bw, bv, br, io, bn, bg, bf, bi, kh, cm, cv, ky, cf, td, cl, cn, cx, cc, co, km, cg, cd, ck, cr, ci, cu, cy, cz, dk, dj, dm, ec, eg, sv, gq, er, ee, fk, fo, fj, fi, gf, pf, tf, ga, gm, ge, gh, gi, gr, gd, gp, gu, gt, gg, gn, gw, gy, ht, hm, va, hn, hk, is, in, ir, iq, ie, im, il, jm, jp, je, jo, kz, ke, ki, kp, kw, kg, la, lv, lb, ls, lr, ly, li, lu, mo, mk, mg, mw, my, mv, ml, mt, mh, mq, mr, mu, yt, mx, fm, md, mc, mn, me, ms, ma, mz, mm, na, nr, np, an, nc, nz, ni, ne, ng, nu, nf, mp, no, om, pk, pw, ps, pa, pg, py, pe, ph, pn, pt, pr, qa, re, ro, rw, bl, sh, kn, lc, mf, pm, vc, ws, sm, st, sa, sn, rs, sc, sg, si, sb, so, za, gs, lk, sd, sr, sj, sz, ch, sy, tw, tj, tz, th, tl, tg, tk, to, tt, tn, tm, tc, tv, ug, ua, ae, gb, us, um, uy, uz, vu, ve, vn, vg, wf, eh, ye, zm, zw, ;
	public static List<CountryCode> getSupportedLanguages() {
		return Arrays.asList(ar, ca, cs, da, de, el, es, et, fr, gl, hr, hu, id, it, ja, kr, lt, nb, nl, nn, pl, pt_BR,
		    pt_PT, ru, se, sk, sl, tr, uk_UA, vi, zh_CN, zh_TW);
	}
}
