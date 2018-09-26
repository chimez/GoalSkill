from rest_framework import mixins, status, viewsets
from rest_framework.decorators import action
from rest_framework.response import Response

from .models import (Level, Method, MethodFamily, Skill, SkillFamily, User,
                     UserMethod, UserSkill)
from .serializers import (
    AllSkillSerializer, LevelSerializer, MethodFamilySerializer,
    MethodSerializer, SkillFamilySerializer, SkillMethodSerializer,
    SkillSerializer, UserMethodSerializer, UserSerializer, UserSkillSerializer)


class UserSkillFamilyViewSet(viewsets.ReadOnlyModelViewSet):
    """
    list:
    返回 me 用户下所有的技能家族 SkillFamily 的 id 和 name

    read:
    返回指定 id 的用户的相关信息

    all_skill:
    返回 me 用户下家族 id 为 pk 的所有技能的信息, skill_family_id 是技能家族的 id 而不是用户 id
    """
    queryset = User.objects.all()
    serializer_class = UserSerializer

    def list(self, request):
        me = User.objects.get(name="me")
        skill_families = me.skill_family.all()
        serializer = SkillFamilySerializer(skill_families, many=True)
        return Response(serializer.data)

    def read(serf, request, pk=None):
        user = User.objects.get(pk=pk)
        skill_families = user.skill_family.all()
        serializer = SkillFamilySerializer(skill_families, many=True)
        return Response(serializer.data)

    def all_skill(self, request, pk=None, skill_family_id=None):
        user = User.objects.get(pk=pk)
        skill_family = SkillFamily.objects.get(pk=skill_family_id)
        all_skill = UserSkill.objects.filter(
            user=user, skill__skill_family=skill_family)
        serializer = AllSkillSerializer(all_skill, many=True)
        return Response(serializer.data)


class SkillMethodViewSet(mixins.RetrieveModelMixin, viewsets.GenericViewSet):
    """
    read:
    给出 me 用户的 id 技能的所有方法的信息

    inc_method_times:
    用户技能总编号为 id 的加1
    """
    queryset = UserMethod.objects.all()
    serializer_class = SkillMethodSerializer

    def read(self, request, pk=None):
        me = User.objects.get(name="me")
        skill = Skill.objects.get(pk=pk)
        methods = UserMethod.objects.filter(user=me, skill=skill)
        serializer = SkillMethodSerializer(methods, many=True)
        return Response(serializer.data)

    @action(detail=True)
    def inc_method_times(self, request, pk=None):
        method = UserMethod.objects.get(pk=pk)
        method.method_times += 1
        method.save()
        user_skill = UserSkill.objects.filter(user=method.user).filter(
            skill=method.skill)[0]
        user_skill.skill_exp = method.method_times * method.method.exp
        user_skill.save()
        levels = {}
        level_exp_array = []
        for level in Level.objects.all():
            levels[level.need_exp] = level
            level_exp_array.append(level.need_exp)
        level_exp_array.sort()
        now_level_exp = list(
            filter(lambda x: x > user_skill.skill_exp, level_exp_array))[0]
        now_level = levels[now_level_exp]
        user_skill.skill_level = now_level
        user_skill.save()
        return Response(status.HTTP_200_OK)
