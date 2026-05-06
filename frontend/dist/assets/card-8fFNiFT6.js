import{At as e,Bt as t,Dt as n,Lt as r,Ut as i,d as a,h as o,kt as s}from"./portal-dUFiXB_z.js";var c={name:`Card`,extends:{name:`BaseCard`,extends:a,style:o.extend({name:`card`,style:`
    .p-card {
        background: dt('card.background');
        color: dt('card.color');
        box-shadow: dt('card.shadow');
        border-radius: dt('card.border.radius');
        display: flex;
        flex-direction: column;
    }

    .p-card-caption {
        display: flex;
        flex-direction: column;
        gap: dt('card.caption.gap');
    }

    .p-card-body {
        padding: dt('card.body.padding');
        display: flex;
        flex-direction: column;
        gap: dt('card.body.gap');
    }

    .p-card-title {
        font-size: dt('card.title.font.size');
        font-weight: dt('card.title.font.weight');
    }

    .p-card-subtitle {
        color: dt('card.subtitle.color');
    }
`,classes:{root:`p-card p-component`,header:`p-card-header`,body:`p-card-body`,caption:`p-card-caption`,title:`p-card-title`,subtitle:`p-card-subtitle`,content:`p-card-content`,footer:`p-card-footer`}}),provide:function(){return{$pcCard:this,$parentInstance:this}}},inheritAttrs:!1};function l(a,o,c,l,u,d){return t(),e(`div`,r({class:a.cx(`root`)},a.ptmi(`root`)),[a.$slots.header?(t(),e(`div`,r({key:0,class:a.cx(`header`)},a.ptm(`header`)),[i(a.$slots,`header`)],16)):s(``,!0),n(`div`,r({class:a.cx(`body`)},a.ptm(`body`)),[a.$slots.title||a.$slots.subtitle?(t(),e(`div`,r({key:0,class:a.cx(`caption`)},a.ptm(`caption`)),[a.$slots.title?(t(),e(`div`,r({key:0,class:a.cx(`title`)},a.ptm(`title`)),[i(a.$slots,`title`)],16)):s(``,!0),a.$slots.subtitle?(t(),e(`div`,r({key:1,class:a.cx(`subtitle`)},a.ptm(`subtitle`)),[i(a.$slots,`subtitle`)],16)):s(``,!0)],16)):s(``,!0),n(`div`,r({class:a.cx(`content`)},a.ptm(`content`)),[i(a.$slots,`content`)],16),a.$slots.footer?(t(),e(`div`,r({key:1,class:a.cx(`footer`)},a.ptm(`footer`)),[i(a.$slots,`footer`)],16)):s(``,!0)],16)],16)}c.render=l;export{c as t};